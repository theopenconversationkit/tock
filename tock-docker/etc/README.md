# Deploy in maven repository

## Local configuration

Copy/paste the contents of [etc/deploy-settings.xml](https://github.com/theopenconversationkit/tock-docker/blob/master/etc/deploy-settings.xml)
 in your .m2/settings.xml - set the login/password of your docker hub account

## How to deploy a snapshot

In the root directory of the project, run 
 
```sh 
    etc/pushSnapshot.sh
```  

## How to release and deploy a release

In the root directory of the project, run 
 
```sh 
    etc/releaseAndPush.sh [release version]
```  



  

 