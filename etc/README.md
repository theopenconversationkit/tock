# How to build intermediate build

Add a git tag using the convention "build-$version-$buildNumber"

> Example : git tag build-19.9.1-10 && git push origin --tags

The build is automatically uploaded by travis to https://dl.bintray.com/tock/tock

> Don't forget to tag also https://github.com/theopenconversationkit/tock-corenlp project if you need it

# Deploy in maven repository

## Local configuration

Copy/paste the contents of [etc/deploy-settings.xml](https://github.com/theopenconversationkit/tock/blob/master/etc/deploy-settings.xml)
 in your .m2/settings.xml - set the login/password of your sonatype account
 and the keyname and passpharse of your gpg key

## How to deploy a snapshot

In the root directory of the project, run 
 
```sh 
    etc/deploySnapshot.sh
```  

## How to release and deploy a release

In the root directory of the project, run 
 
```sh 
    etc/releaseAndDeploy.sh
```  



  

 