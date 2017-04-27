# Deploy in maven repository

## Local configuration

Copy/paste the contents of [etc/deploy-settings.xml](https://github.com/voyages-sncf-technologies/tock/blob/master/etc/deploy-settings.xml)
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



  

 