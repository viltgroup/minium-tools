<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

	<modelVersion>4.0.0</modelVersion>

	<parent>
		<artifactId>spring-boot-starter-parent</artifactId>
		<groupId>org.springframework.boot</groupId>
		<version>1.2.5.RELEASE</version>
		<relativePath />
	</parent>

	<groupId>${groupId}</groupId>
	<artifactId>${artifactId}</artifactId>
	<version>${version}</version>

	<properties>
		<java.version>1.7</java.version>

		<minium.version>${miniumVersion}</minium.version>
		<selenium.version>2.45.0</selenium.version>
		<spring.version>${springVersion}</spring.version>
		<!-- cucumber 1.20 doesn't seem to support 4.12 yet -->
		<junit.version>4.11</junit.version>
		
		<failsafe.skip.tests>false</failsafe.skip.tests>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.seleniumhq.selenium</groupId>
			<artifactId>selenium-java</artifactId>
			<version>${r"${selenium.version}"}</version>
			<exclusions>
				<exclusion>
					<artifactId>selenium-htmlunit-driver</artifactId>
					<groupId>org.seleniumhq.selenium</groupId>
				</exclusion>
			</exclusions>
		</dependency>
		<dependency>
			<groupId>ch.qos.logback</groupId>
			<artifactId>logback-classic</artifactId>
		</dependency>
		<dependency>
			<groupId>io.vilt.minium</groupId>
			<artifactId>minium-cucumber</artifactId>
			<version>${r"${minium.version}"}</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<configuration>
					<skipTests>true</skipTests>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-failsafe-plugin</artifactId>
				<configuration>
					<skipTests>${r"${failsafe.skip.tests}"}</skipTests>
				</configuration>
				<executions>
					<execution>
						<goals>
							<goal>integration-test</goal>
							<goal>verify</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

	<repositories>
		<repository>
			<id>sonatype-snapshot</id>
			<url>http://oss.sonatype.org/content/repositories/snapshots</url>
			<releases>
				<enabled>false</enabled>
			</releases>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
	</repositories>

</project>
