package minium.pupino.jenkins;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import minium.pupino.web.rest.dto.BrowsersDTO;
import minium.pupino.web.rest.dto.BuildDTO;
import minium.pupino.web.rest.dto.SummaryDTO;
import net.masterthought.cucumber.json.Feature;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Artifact;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;

public class JenkinsClientAdaptor implements JenkinsClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsClientAdaptor.class);

	@Autowired
	private JenkinsServer jenkins;
	private JenkinsJobConfigurator jobConfigurator;
	private ReporterParser reporter = new ReporterParser();


	public JenkinsClientAdaptor() throws URISyntaxException {
        jobConfigurator = new JenkinsJobConfigurator();
	}

	/**
	 * return the job with details
	 */
	@Override
	public JobWithDetails uri(String jobName) throws IOException, URISyntaxException {
		JobWithDetails job = jenkins.getJob(jobName);
		return job;
	}

	/*
	 * JOBS
	 */

	@Override
	public void createJob(String jobName, String scmType, String repository) throws JAXBException, IOException {
		String sourceXml = jobConfigurator.getXMLSource(scmType, repository);
		jenkins.createJob(jobName, sourceXml);

	}

	@Override
	public void updateJobConfiguration(String jobName, BrowsersDTO buildConfig) {
		try {
			String jobConfig = jenkins.getJobXml(jobName);
			LOGGER.info("Job configuration {} updated" + jobConfig);
			// change the xml config
			String updatedXml = jobConfigurator.updateJobConfig(jobConfig, "goals", buildConfig);
			// update the job with the new configuration in jenkins
			jenkins.updateJob(jobName, updatedXml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public JobWithDetails getJob(String name) throws IOException {
		return jenkins.getJob(name);
	}

	/*
	 * BUILDS
	 */

	@Override
	public JobWithDetails createBuild(String jobName, BrowsersDTO buildConfig, boolean updatedConfig) throws IOException, URISyntaxException {
		// jobName = "server";
		JobWithDetails job = jenkins.getJob(jobName);
		if (updatedConfig)
			this.updateJobConfiguration(jobName, buildConfig);
		job.build();
		return job;
	}

	@Override
	public List<Build> buildsForJob(String jobName) throws IOException, URISyntaxException {
		return jenkins.getJob(jobName).getBuilds();
	}

	@Override
	public Build lastCompletedBuild(String jobName) throws IOException {
		return jenkins.getJob(jobName).getLastCompletedBuild();
	}

	@Override
	public Build lastBuild(String jobName) throws IOException {
		return jenkins.getJob(jobName).getLastBuild();
	}

	/*
	 * ARTIFACTS
	 */
	@Override
	public String getArtifactsBuild(BuildWithDetails buildDetails) {
	    Reader in = null;
	    String result = "";
	    try {
    		if (!buildDetails.getArtifacts().isEmpty()) {
    			Artifact artifact = buildDetails.getArtifacts().get(0);
    			// function from the jenkins client was not working properly use
    			if (artifact.getDisplayPath().equals("result.json")) {
    			    String artifactPath = buildDetails.getUrl() +"artifact/"+  artifact.getRelativePath();
                    in = new InputStreamReader(new URL(artifactPath).openStream(), Charsets.UTF_8);
    			} else {
    			    in = new FileReader("mocks/mock-cgd-store.json");
    			}
    			result =  IOUtils.toString(in);
    		}
    		
	    } catch (IOException e) {
            throw Throwables.propagate(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
	    return result;
	}

	private String getStatusForBuild(BuildWithDetails bd) {
		return (bd.getResult() != null) ? bd.getResult().toString() : "BUILDING";
	}

	/*
	 * This methods are not used anymore
	 */
	@Override
	public List<BuildDTO> getBuilds(String jobName) throws IOException, URISyntaxException {
		List<Build> builds = buildsForJob(jobName);
		List<BuildDTO> buildsDTO = Lists.newArrayList();
		BuildDTO buildDTO;
		BuildWithDetails bd;
		boolean lastBuild = true;
		SummaryDTO summary = null;
		String artifact = "";
		for (Build b : builds) {
			bd = b.details();
			String result = getStatusForBuild(bd);
			// only want the report of the lastBuild finished
			// if (lastBuild && !bd.isBuilding()) {
			// get the artifact of the build and return the string
			artifact = getArtifactsBuild(bd);
			lastBuild = false;
			List<Feature> features = reporter.parseJsonResult(artifact);
			summary = reporter.getSummaryFromFeatures(features);
			buildDTO = new BuildDTO(1, b.getNumber(), b.getUrl(), bd.isBuilding(), bd.getDescription(), bd.getDuration(), bd.getFullDisplayName(), bd.getId(),
					bd.getTimestamp(), artifact, features, summary, "");
			// } else {
			// summary = null;
			// buildDTO = new BuildDTO(b.getNumber(), b.getUrl(),
			// bd.getActions(), bd.isBuilding(), bd.getDescription(),
			// bd.getDuration(),
			// bd.getFullDisplayName(), bd.getId(), bd.getTimestamp(), result,
			// summary);
			// }
			buildsDTO.add(buildDTO);
		}
		return buildsDTO;
	}

	@Override
	public BuildDTO getBuildById(String jobName, String buildId) throws JsonSyntaxException, JsonIOException, IOException, URISyntaxException {
		List<Build> builds = buildsForJob(jobName);
		BuildDTO buildDTO = null;
		BuildWithDetails bd;
		boolean lastBuild = true;
		String artifact = "";
		for (Build b : builds) {
			bd = b.details();
			String result = getStatusForBuild(bd);
			// only want the report of the lastBuild finished
			if (lastBuild && !bd.isBuilding()) {
				// get the artifact of the build and return the string
				artifact = getArtifactsBuild(bd);
				lastBuild = false;
				List<Feature> features = reporter.parseJsonResult(artifact);
				for (Feature f : features) {
					f.processSteps();
				}
				buildDTO = new BuildDTO(1, b.getNumber(), b.getUrl(), bd.isBuilding(), bd.getDescription(), bd.getDuration(), bd.getFullDisplayName(),
						bd.getId(), bd.getTimestamp(), artifact, features, null, "");
			}

		}
		return buildDTO;
	}

	/**
	 * Get a specific build
	 *
	 * @param jobName
	 * @param buildId
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Override
	public BuildDTO getBuildAndFeature(String jobName, String buildId, String featureURI) throws IOException, URISyntaxException {
		List<Build> builds = buildsForJob(jobName);
		BuildDTO buildDTO = null;
		BuildWithDetails bd;
		boolean lastBuild = true;
		for (Build b : builds) {
			String artifact = "";
			bd = b.details();
			if (bd.getId().equals(buildId)) {
				String result = getStatusForBuild(bd);
				// only want the report of the lastBuild finished
				if (lastBuild && !bd.isBuilding()) {
					// get the artifact of the build and return the string
					artifact = getArtifactsBuild(bd);
					lastBuild = false;
					Map<String, Feature> features = reporter.parseJsonResultSet(artifact);
					Feature f = features.get(featureURI);
					f.processSteps();
					buildDTO = new BuildDTO(1, b.getNumber(), b.getUrl(), bd.isBuilding(), bd.getDescription(), bd.getDuration(), bd.getFullDisplayName(),
							bd.getId(), bd.getTimestamp(), artifact, Arrays.asList(f), null, "");
				}
			}
		}
		return buildDTO;
	}

}
