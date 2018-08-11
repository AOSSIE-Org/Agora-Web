# Agora REST API

## Student - Abanda Ludovic
## Links  
- Project : https://gitlab.com/aossie/Agora-Web
- Live demo of the Project :  http://agora-rest-api.herokuapp.com/
- Wiki page : [Wiki](../../wiki-2018.md)

## Agora Backend (REST API)  

The goal of the project is to divide the old agora platform into a REST API and a web frontend. This part of the project was to produce a REST API(backend) for the Agora platform. I made use of the models that were already found in the code base and also reused as much code as I could. The following parts will describe the project in details.

### Use case modeling 

I have identified the following tasks in the project at the starting of the project.
1. Endpoints for user signup and login with email base accounts - **Done**
2. Endpoints for user login using social media account - **Done** 
3. Endpoints for user to logout. - **Done** 
4. Endpoints for user to create and schedule Election.  - **Done** 
5. Endpoints for user to edit Election.  - **Done** 
6. Endpoints for user to delete the Election. - **Done** 
7. Endpoints for election creator to invite the voter to the Election. - **Done** 
8. System will inform the voter with the link to vote the Election.  - **Done**
7. System will handle the timeline of the Election. **Done**
8. System will count the votes and produce the results for the Election after the Election. - **Done** 
9. Voter can vote the Election. - **Done** 

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

We started the Google summer of code by defining all backend specifications as [Swagger](https://swagger.io/docs/specification/2-0/basic-structure/) specs. Configured the project using play framework 2.6 seeds and integrated swagger UI then deploying it in the cloud. We used Heroku as the Cloud option. We have made available the prototype user interfaces in the Heroku cloud. After that, we have started to work with abstract schema of the models and create controllers to handle actions on those models. Then we started to work with the user authentication part. We use silhouette as our authentication library. After that we spent time to configure the Silhouette module and created email based signup and signin endpoints after which we worked on authentication with social providers still using silhouette. Finally we have built JWT token based authentication and authorization system for OAuth2 and email based login. Apart from that we created endpoints for user to logout, update their account and change their password.

After that we started working on the election services and models.Then we searched a cloud solution for MongoDB. We use Mlab as our cloud database and found it to be great for development. We used [Play2-ReactiveMongoDB](http://reactivemongo.org/releases/0.1x/documentation/tutorial/play.html) to connect to mongoDB in order to store and get our election and user data. After which we created controllers and endpoints to create, edit, delete and get election data.

 
After which we created endpoints to verify voters identity and for voters to vote for elections. We have used the [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) (Advanced encryption standard) for creating the `passcode` for the voter. We used [Play-Mailer](https://github.com/playframework/play-mailer) with Sendgrid to send the emails to voters. Then we started to implement the vote counting function using [Agora](https://gitlab.com/aossie/Agora) library and saved the data into the database.

We created results endpoints to serve the results for finished elections or elections that have support for real-time results.
All these endpoints are available at [demo](http://agora-rest-api.herokuapp.com/) with basic swagger documentation describing each endpoints. I discussed a lot with my mentors on each step that was to be taken and they have always helped me with valuable guidance on what is needed.Though the API supports various features I can't really say it is production ready since we have to test it with real users and observe how it responds. We worked on the API documentation as we were building the API due to swagger integration. So each new endpoint that was created was accompanied with its documentation, we have many things to improve in documentation and also in the Agora API.

I would like to thank every AOSSIE member, especially my mentors, Thuvarakan Tharmarajasingam, Bruno Woltzenlogel Paleo and Ezequiel Postan for being so nice and helpful. I have learnt a lot in the past 3 months and it has been a great experience to be a part of this wonderful community. 

### Merge Requests 
1. [ Merge request !1](https://gitlab.com/aossie/Agora-Web/merge_requests/34) - Upgraded to play 2.6.12 and added swagger specs: - status *Merged*
    * Upgraded old play framework app from version 2.5 to 2.6.
    * Integrated swagger  UI
    * Implemented email base signup and sign in endpoints

2. [Merge request !2](https://gitlab.com/aossie/Agora-Web/merge_requests/35) - Imported models from old codebase and made the required changes.: - status *Merged*
    *  Imported required models and services from old code base and added some too

3. [Merge request !3](https://gitlab.com/aossie/Agora-Web/merge_requests/36) - Added user actions  - Status: *Merged*
    *  Implemented user account actions such as logout, change password, change profile info and get user data
    *  Host development branch on Heroku 

4. [Merge request !4](https://gitlab.com/aossie/Agora-Web/merge_requests/39) - Social authentication - Status: *Merged*
    * Implementing Social authentication with OAuth2 social providers such as Facebook and Google. (edited)

5. [Merge request !5](https://gitlab.com/aossie/Agora-Web/merge_requests/38) - Account verification - Status: *Merged*
    * Implemented user account verification endpoints
    * Implemented means for user to reset password if forgotten.

6. [Merge request !6](https://gitlab.com/aossie/Agora-Web/merge_requests/40) - Election endpoints - Status: *Merged*
    * Implemented elections endpoints
    * Users could now create, edit, delete and view elections
    * Implemented endpoints for users to add voters.
    * Implemented endpoint for users to vote.

7. [Merge request !7](https://gitlab.com/aossie/Agora-Web/merge_requests/41) – Voting Result endpoints - Status: *Merged*
    * Implemented endpoint to get election results

8. [Merge request !8](https://gitlab.com/aossie/Agora-Web/merge_requests/42) - Major improvements - Status: *Merged*
    * Let dates be in UTC format so that users with different time zones will have the correct time shown to them.
    * Return user information and token during login instead of only token to prevent second Rest call to get user data
    * Add other ballot parsers (before we supported only Preferential ballot parsing). Now we support all ballot types
    * Add other elections that depends on the new Ballots supported