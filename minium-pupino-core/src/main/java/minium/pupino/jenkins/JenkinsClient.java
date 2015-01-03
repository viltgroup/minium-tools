package minium.pupino.jenkins;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.JAXBException;

import minium.pupino.web.rest.dto.BrowsersDTO;
import minium.pupino.web.rest.dto.BuildDTO;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;

public interface JenkinsClient {

	JobWithDetails uri(String jobName) throws IOException, URISyntaxException;

	/*
	 * JOBS
	 */
	void createJob(String jobName, String scmType, String repository) throws IOException, JAXBException;

	void updateJobConfiguration(String jobName, BrowsersDTO buildConfig);

	/*
	 * BUILDS
	 */
	List<Build> buildsForJob(String jobName) throws IOException, URISyntaxException;

	Build lastBuild(String jobName) throws IOException;

	void createBuild(String jobName,BrowsersDTO buildConfig) throws IOException, URISyntaxException;

	List<BuildDTO> getBuilds(String jobName) throws IOException, URISyntaxException;

	BuildDTO getBuildAndFeature(String jobName, String buildId, String featureURI) throws IOException, URISyntaxException;

	BuildDTO getBuildById(String jobName, String buildId) throws JsonSyntaxException, JsonIOException, FileNotFoundException, IOException, URISyntaxException;

	/*
	 * ARTIFACTS
	 */
	String getArtifactsBuild(BuildWithDetails buildDetails);

	

}