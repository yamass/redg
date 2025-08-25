# RedG Deployment Guide

This is a step-by-step guide on how to deploy RedG to Maven Central vie the Sonatype Repository.

## Important links for more information

 - [The OSSRH guide](http://central.sonatype.org/pages/ossrh-guide.html)

## Prerequisites

 - Have GPG installed and have an RSA key where the public key is published to the most common key servers (at least to hkp://pool.sks-keyservers.net)
 - Have a Sonatype Jira Account with permissions to deploy for `de.yamass.redg`
 
## Configuration files

There is some stuff you need in your `~/.m2/settings.xml`:

````xml
<settings ...>
	<!-- ... -->
	<servers>
		<server>
			<id>gpg.passphrase</id>
			<passphrase>YOUR_GPG_KEY_PW_HERE</passphrase>
		</server>
		<server>
			<!-- The credentials for Sonatype OSSRH Jira -->
			<id>ossrh</id>
			<username>username-here</username>
			<password>pw-here</password>
		</server>
		<server>
			<!-- The credentials for GitHub here (If you have enabled 2FA, generate an peronal access token and use it instead) -->
			<id>github</id>
			<username>username-here</username>
			<password>pw-here</password>
		</server>
	</servers>
	<!-- ... -->
	<profiles>
        <id>ossrh</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <gpg.executable>gpg2</gpg.executable>
			<gpg.passphrase>YOUR_GPG_KEY_PW_HERE_AGAIN</gpg.passphrase>
			<gpg.keyname>GPG_KEY_NAME_HERE</gpg.keyname>
        </properties>
	</profiles>
</settings>
````

## Test the build

Before you even try to perform a release, test that everything is configured properly and working by running `mvn clean install -P release`. 
This tests the GPG signing and the Javadoc and builds all required artifacts for Sonatype.

## Build and release

Run `mvn release:clean release:prepare` to prepare the release and answer all the questions concering the version numbers.

Now you can perform the actual release by running `mvn release:perform`.
If no errors occur, the release will be available on Central in the next few hours.