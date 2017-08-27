# Agora Web

## Student - Thuvarakan Tharmarajasingam
## Links  
- Project : https://gitlab.com/aossie/Agora-Web
- Live demo of the Project :  https://agora-web-aossie.herokuapp.com
- Wiki page : [Wiki](/docs/wiki.md)
- Project tag : 

## Agora Web  

The goal of the project to develop a front end for the AGORA project. Since AGORA project does not have a web interface, project should be started from the scratch.  Following parts will describe the project in details.

### Use case modelling 

I have identified following use cases in the project at the starting of the project. 
1. User can login and logout - **Done** 
2. User/Guest can create and schedule election  - **Done** 
3. User/Guest can end/edit election  - **Done** 
4. User/Guest can delete the election - **Done** 
5. Election creator can invite voter to the election. - **Done** 
6. System will inform the voter with the passcode to vote the Election.  - **Done**
7. System will handle the timeline of the election. **Done**
8. System will count the voters and produce the results for the election after the election - **Done** 
9. Voter vote the election - **Done** 

### Deep view into the technology 

This project is created using play framework 2.5 seeds [template](https://github.com/playframework/play-scala-seed.g8).

These some of the main technologies, We have used in the project,

* [sbt](http://www.scala-sbt.org/) - Build tool for scala.
* [Play framework](https://www.playframework.com/) - The Web framework is used build the project.
* [Scala](https://www.scala-lang.org/) - Langugae which is used to write the server site.
* [MongoDB](https://docs.mongodb.com/) - Database for the project. 
* [Silhouette Documentation](https://www.silhouette.rocks/docs) - Handle the user authentication and autherization for the project
* [Casbah MongoDB](https://mongodb.github.io/casbah/) - Used to connect with the MongoDB
* [Salat](https://github.com/salat/salat) - Used to serialize the case classes into the BSON data.
* [specs2](https://github.com/etorreborre/specs2) - Used to unit tests.

We started the Google summer of code with configure fresh project using play framework 2.5 seeds and deploy it in the cloud. We used Heroku as the Cloud option. We have a made available the prototype user interfaces in the heroku cloud. After that We had stated to work with abstract schema of the models and create controllers to handle acotions on those models. Then We were starting to work with the user authentication part, We had two options for the authentication module. Those are Silhouette, SecureSocial . One of my mentor suggest me to go with Silhouette. After that We spent time to configure the Silhouette module and make the Oauth2 authentication to connect with the social accounts. Finally We have built the token based Oauth2 authentication and authorization. Following diagram will explain how the authetication works,

![webflow](https://developers.google.com/accounts/images/webflow.png)
*Get from Google Developer page*

After that, We created the `create election function` for users/guests and save the results the in the local database. Then We search a cloud solution for MongoDB. We found mLab MongoDB in the Heroku addons. We integreated the cloud database into the system. Then We spent time to create interface for each ballot, In the view we have used the default template engine [Twirl](https://github.com/playframework/twirl) for the Play framework. 

We have used the [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) (Advanced encryption standard) for creating the `passcode` for the voter. We used [Play-Mailer](https://github.com/playframework/play-mailer) with Sendgrid to send the emails to voters and guests. Then we started to implement the vote counting function using [Agora](https://gitlab.com/aossie/Agora) library and save the data into the database. We had some problems while converting the ballot data from the database to the format which Agora library undersatand. My mentors helped me to get pass the situation with their guidance. 

Then We started to polish the sytem and took more concern about the basic privacy aspects of the Election, Users and Voters. Defining the privacy aspects was also a dificult part, my mentors guided me in that process. System is now in the beta stage we have to test it with real users to polish it to make it production ready. We worked more with the documentation for the last week of the Coding period, Still We have many things to improve the documnetation and also in the Agora web.

I would like to thank every AOSSIE member, especially my mentors, Bruno Woltzenlogel Paleo, Ekaterina Lebedeva, Daniyar Itegulov, for being so nice and helpful. I have learnt a lot in the past 3 months and it has been a great experience to be a part of this wonderful community. 

### Merge Requests 
1. [ Merge request !1](https://gitlab.com/aossie/Agora-Web/merge_requests/1) - Initialize the project for agora-web - Status: *Merged*
    *  Created fresh web project using play framework 2.5 seeds templates and included sample user interfaces.

2. [Merge request !2](https://gitlab.com/aossie/Agora-Web/merge_requests/2) - Webjar added to the project - Status: *Closed*
    *  Removed the css file from the project and configure project using webjar. 
    *  Removed the gradle configuration file.

3. [Merge request !3](https://gitlab.com/aossie/Agora-Web/merge_requests/3) - Start to Create back end for add election functionality - Status: *Merged*
    *  Created a model class - Election 
    *  Created a Controller for Election 
    *  Created a view form using form helper.
    *  Created a model class - User
    *  User authentication
    *  Create election function

4. [Merge request !4](https://gitlab.com/aossie/Agora-Web/merge_requests/4) - Election view - Status: *Closed*
    *  Connected with remote database
    *  Create election in database
    *  Added the election data to profile 
    *  Start to develop the view of the single election.

5. [Merge request !5](https://gitlab.com/aossie/Agora-Web/merge_requests/5) - Design voting interface - Status: *Closed*
    * Starting to develop ballot interface for the algorithms.

6. [Merge request !6](https://gitlab.com/aossie/Agora-Web/merge_requests/6) - Developing the view for the election and user profile - Status: *Merged*
    *  Interface for election
    *  Interface for user

7. [Merge request !7](https://gitlab.com/aossie/Agora-Web/merge_requests/7) - Developing interface for each ballot - Status: *Merged*
    * CreateSingle candidate ballot interface
    * Create approval Ballot interface
    * Create scored Ballot interface
    * Create new add election interface
    * Create preferential with indifference ballot interface

9. [Merge request !9](https://gitlab.com/aossie/Agora-Web/merge_requests/9) - Sending pass code for voter - Status: *Merged*
    * Create add voter function
    * Created the sending passcode to voter function. 
    * passcode verification parr
    * Implement the visibility level for ballot, result and voter

10. [Merge request !10](https://gitlab.com/aossie/Agora-Web/merge_requests/10) - Counting votes - Status: *Merged*
    * created the methods to add result text into the database and receive it.
    * Redesign the Create election view
    * Implement the Count election function
    * Implement the election sheduling in the system 

11. [Merge request !11](https://gitlab.com/aossie/Agora-Web/merge_requests/11) - Guest admin link - Status: *Merged*
    * Fix the guest election creation and admin link part

12. [Merge request !12](https://gitlab.com/aossie/Agora-Web/merge_requests/12) - Fix the edit election and add the number of vacancies - Status: *Merged*
    * Fix various bugs in the system. 

13. [Merge request !13](https://gitlab.com/aossie/Agora-Web/merge_requests/13) - Show the voted elections in the user profile - Status: *Open*
    * Redesign the user profile view

14. [Merge request !14](https://gitlab.com/aossie/Agora-Web/merge_requests/14) - Edit the add election interface and refactor code - Status: *Merged*
    * Add wiki pages for algorithms
    * Change the dropdowns to Radio buttons

15. [Merge request !15](https://gitlab.com/aossie/Agora-Web/merge_requests/15) - Update Readme file - Status: *Merged*
    * Create the Readme file.