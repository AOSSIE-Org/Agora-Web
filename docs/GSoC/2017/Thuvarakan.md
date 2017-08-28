# Agora Web

## Student - Thuvarakan Tharmarajasingam
## Links  
- Project : https://gitlab.com/aossie/Agora-Web
- Live demo of the Project :  https://agora-web-aossie.herokuapp.com
- Wiki page : [Wiki](docs/wiki.md)

## Agora Web  

The goal of the project is to develop a front end for the AGORA project. Since AGORA project does not have a web interface, project had to be started from the scratch.  Following parts will describe the project in details.

### Use case modeling 

I have identified the following use cases in the project at the starting of the project. 
1. User can login and logout. - **Done** 
2. User/Guest can create and schedule Election.  - **Done** 
3. User/Guest can end/edit Election.  - **Done** 
4. User/Guest can delete the Election. - **Done** 
5. Election creator can invite the voter to the Election. - **Done** 
6. System will inform the voter with the passcode to vote the Election.  - **Done**
7. System will handle the timeline of the Election. **Done**
8. System will count the voters and produce the results for the Election after the Election. - **Done** 
9. Voter can vote the Election. - **Done** 

### Deep view into the technology. 

This project is created using play framework 2.5 seeds [template](https://github.com/playframework/play-scala-seed.g8).

These are some of the main technologies, we have used in the project.

* [sbt](http://www.scala-sbt.org/) - Build tool for Scala.
* [Play framework] (https://www.playframework.com/) - The Web framework is used to build the project.
* [Scala](https://www.scala-lang.org/) - Language which is used to write the server site.
* [MongoDB](https://docs.mongodb.com/) – Database used for the project. 
* [Silhouette Documentation](https://www.silhouette.rocks/docs) - Handle the user authentication and authorization for the project.
* [Casbah MongoDB](https://mongodb.github.io/casbah/) - Used to connect with the MongoDB
* [Salat](https://github.com/salat/salat) - Used to serialize the case classes into the BSON data.
* [specs2](https://github.com/etorreborre/specs2) - Used to unit tests.

We started the Google summer of code with configuring fresh project using play framework 2.5 seeds and deploying it in the cloud. We used Heroku as the Cloud option. We have made available the prototype user interfaces in the Heroku cloud. After that, we have started to work with abstract schema of the models and create controllers to handle actions on those models. Then we started to work with the user authentication part. We had two options for the authentication module. Those are Silhouette and SecureSocial . One of my mentors suggested me to go with Silhouette. After that we spent time to configure the Silhouette module and made the Oauth2 authentication to connect with the social accounts. Finally we have built the token based Oauth2 authentication and authorization. Following diagram will explain how the authentication works.

![webflow](https://developers.google.com/accounts/images/webflow.png)
*From Google Developer page*

After that, we created the `create election function` for users/guests and saved the results in the local database. Then we searched a cloud solution for MongoDB. We found mLab MongoDB in the Heroku addons. We integrated the cloud database into the system. Then we spent time to create interface for each ballot. In the view we have used the default template engine [Twirl](https://github.com/playframework/twirl) for the Play framework. 

We have used the [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) (Advanced encryption standard) for creating the `passcode` for the voter. We used [Play-Mailer](https://github.com/playframework/play-mailer) with Sendgrid to send the emails to voters and guests. Then we started to implement the vote counting function using [Agora](https://gitlab.com/aossie/Agora) library and saved the data into the database. We had some problems while converting the ballot data from the database to the format which Agora library can understand. My mentors helped me to get through the situation with their guidance. 

Then we started to polish the system and took more concern about the basic privacy aspects of the Election, Users and Voters. Defining the privacy aspects was also a difficult part, my mentors guided me in that process. System is now at the beta stage and we have to test it with real users to polish it to make this product ready. We worked more with the documentation in the last week of the Coding period, still we have many things to improve in documentation and also in the Agora web.

I would like to thank every AOSSIE member, especially my mentors, Bruno Woltzenlogel Paleo, Ekaterina Lebedeva, Daniyar Itegulov, for being so nice and helpful. I have learnt a lot in the past 3 months and it has been a great experience to be a part of this wonderful community. 

### Merge Requests 
1. [ Merge request !1](https://gitlab.com/aossie/Agora-Web/merge_requests/1) - Initialized the project for agora-web - Status: *Merged*
    * Created fresh web project using play framework 2.5 seeds templates and included sample user interfaces.

2. [Merge request !2](https://gitlab.com/aossie/Agora-Web/merge_requests/2) - Webjar added to the project - Status: *Closed*
    * Removed the css file from the project and configure project using Webjar. 
Removed the gradle configuration file.

3. [Merge request !3](https://gitlab.com/aossie/Agora-Web/merge_requests/3) - Started to Create back end for add election functionality - Status: *Merged*
    * Created a model class – Election. 
    * Created a Controller for Election. 
    * Created a view form using form helper.
    * Created a model class – User.
    * User authentication.
    * Created election function.

4. [Merge request !4](https://gitlab.com/aossie/Agora-Web/merge_requests/4) - Election view - Status: *Closed*
 * Connected with remote database.
 * Created election in database.
 * Added the election data to profile. 
 * Started to develop the view of the single election.

5. [Merge request !5](https://gitlab.com/aossie/Agora-Web/merge_requests/5) - Designed voting interface - Status: *Closed*
    * Startied to develop ballot interface for the algorithms.

6. [Merge request !6](https://gitlab.com/aossie/Agora-Web/merge_requests/6) - Developed the view for the election and user profile - Status: *Merged*
    * Interface for election.
    * Interface for user.

7. [Merge request !7](https://gitlab.com/aossie/Agora-Web/merge_requests/7) – Developed interface for each ballot - Status: *Merged*
    * Created Single candidate ballot interface.
    * Created approval Ballot interface.
    * Created scored Ballot interface.
    * Created new add election interface.
    * Created preferential with indifference ballot interface.

9. [Merge request !9](https://gitlab.com/aossie/Agora-Web/merge_requests/9) - Sent pass code for voter - Status: *Merged*
    * Created add voter function.
    * Created the sending passcode to voter function. 
    * Passcode verification part.
    * Implemented the visibility level for ballot, result and voter.

10. [Merge request !10](https://gitlab.com/aossie/Agora-Web/merge_requests/10) - Counting votes - Status: *Merged*
    * Created the methods to add result text into the database and receive it.
    * Redesigned the Create election view.
    * Implemented the Count Election function.
    * Implemented the election scheduling in the system. 

11. [Merge request !11](https://gitlab.com/aossie/Agora-Web/merge_requests/11) - Guest admin link - Status: *Merged*
    * Fixed the guest election creation and admin link part.

12. [Merge request !12](https://gitlab.com/aossie/Agora-Web/merge_requests/12) - Fixed the edit election and added the number of vacancies - Status: *Merged*
    * Fixed various bugs in the system. 

13. [Merge request !13](https://gitlab.com/aossie/Agora-Web/merge_requests/13) - Showed the voted elections in the user profile - Status: *Open*
    * Redesigned the user profile view.

14. [Merge request !14](https://gitlab.com/aossie/Agora-Web/merge_requests/14) - Edited the add election interface and refactoring code - Status: *Merged*
    * Added wiki pages for algorithms.
    * Changed the dropdowns to Radio buttons.

15. [Merge request !15](https://gitlab.com/aossie/Agora-Web/merge_requests/15) - Updated Readme file - Status: *Merged*
    * Created the Readme file.

16. [Merge request !16] (https://gitlab.com/aossie/Agora-Web/merge_requests/16) – submission – Status: *Merged*
    * Update the documentation and final report