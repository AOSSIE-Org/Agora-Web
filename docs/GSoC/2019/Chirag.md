# Agora REST API

## Student - Chirag Singhal
## Links  
- Project : https://gitlab.com/aossie/Agora-Web
- Wiki page : [Wiki](../../wiki.md)

## Agora Backend (REST API)  

The goal of the project is to Implement all privacy concerns as described in this document https://civs.cs.cornell.edu/sec_priv.html
and users should be able to add two factor authentication as another security step for login. The system should be able to recognize and track the different devices through which the user has been able to signin with.

### Use case modeling 

I have identified the following tasks in the project at the starting of the project.

### Deep view into the technology. 

This project is created using play framework 2.6 seeds [template](https://github.com/playframework/play-scala-seed.g8).

These are some of the main technologies, we have used in the project.

* [sbt](http://www.scala-sbt.org/) - Build tool for Scala.
* [Play framework](https://www.playframework.com/) - The Web framework is used to build the project.
* [Swagger Play](https://github.com/swagger-api/swagger-play) - Plugin that provides swagger documentation for play framework
* [Scala](https://www.scala-lang.org/) - Language which is used to write the server site.
* [MongoDB](https://docs.mongodb.com/) – Database used for the project. 
* [Silhouette Documentation](https://www.silhouette.rocks/docs) - Handle the user authentication and authorization for the project.
* [Play2-ReactiveMongoDB](http://reactivemongo.org/releases/0.1x/documentation/tutorial/play.html) - Used to connect with the MongoDB
* [specs2](https://github.com/etorreborre/specs2) - Used to unit tests.

We started the Google summer of code by the aim to remove saving of voter's personal information as clear text. Firstly we implemented md5 hashing function and updated voter and ballot classes. I discussed a lot about security concerns with my mentor Abanda Ludovic. After that we implemented not saving voter's email and instead using hash of email and election private key as an identifier for voter.

After that we started working on the second type of election i.e Public Election. We updated election creation process, implemented verify voter links for polls and updated voting process to identify voter on basis of IP address in public elections and save the hash of IP address and private election key to identify user in polls.

After which we started with Two Factor Authentication and implemented RFC 6238 algorithm to generate time based one time password and a shared secret between client and server side. Implemented users to enable and disable two factor authentication and updated user class and get UserData. We implemented verify one time passoword and resend one time password endpoints.

Finally we worked on adding security question instead of one time password in case sometimes users don't have access to their emails. We updated the signup models and added trust my device feature to skip two factor authentication if the device is added as trusted device. After that we fixed a bug in update profile.

I would like to thank every AOSSIE member, especially my mentors, Abanda Ludovic, Bruno Woltzenlogel Paleo and Thuvarakan Tharmarajasingam for being so nice and helpful. I have learnt a lot in the past 3 months and it has been a great experience to be a part of this wonderful community.

### Merge Requests 
1. [ Merge request !1](https://gitlab.com/aossie/Agora-Web/merge_requests/68) - Not saving voter email - status *Merged*
    * Implement md5 hashing function
    * Implemented not saving voter's email instead saving hash of email and election key
    * Updated add voter and voting process 
    * Updated voter and ballot classes

2. [Merge request !2](https://gitlab.com/aossie/Agora-Web/merge_requests/69) - Public Elections - status *Merged*
    * Updated election class
    * Implemented create public election process
    * Implemented voting process for polls, identifying voter's ip address
    * Implemented verify voter link in polls

3. [Merge request !3](https://gitlab.com/aossie/Agora-Web/merge_requests/70) - Two Factor Authentication  - Status: *Open*
    * Implemented time based one time password generation
    * Implemented sending one time password using email 
    * Implemented resend one time password endpoints
    * Implemented user to enable disable two factor authentication endpoints

4. [Merge request !4](https://gitlab.com/aossie/Agora-Web/merge_requests/71) - Security Question and Trust Device - Status: *Open*
    * Updated user class to add security Question
    * Updated user registeration to include security question
    * Implemented get security Question endpoint
    * Implemented login using security question
    * Implemeneted adding verified devices for two factor authentication
    * Updated login endpoint to use trusted devices 
    * Fixed bug in update profile

5. [Merge request !5](https://gitlab.com/aossie/Agora-Web/merge_requests/72) - GSOC docs - Status: *Open*
    * Added gsoc readme file