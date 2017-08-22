# Agora-Web

## Readme

_Frontend for [Agora](https://gitlab.com/aossie/Agora/): An Electronic Voting Library implemented in Scala_


This project is created using play framework 2.5 seeds [template](https://github.com/playframework/play-scala-seed.g8).


![build status](https://gitlab.com/aossie/Agora-Web/badges/master/build.svg)

| Scala | Play | Updated
| :-: | :-: | :-:
| <img src="https://raw.githubusercontent.com/OlegIlyenko/scala-icon/master/scala-icon.png " width="25"> | <img src="https://raw.githubusercontent.com/OlegIlyenko/scala-icon/master/play-icon.png " width="25"> | August 22, 2017

To run the development environment for this frontend you will need to have [Git](https://git-scm.com/), [Sbt](http://www.scala-sbt.org/) and [MongoDB](https://www.mongodb.com/) installed.

## Table of contents

- [Installation](#installation)
- [Running the application](#running-the-application)
- [Troubleshooting your local environment](#troubleshooting-your-local-environment)
- [Testing](#testing)
- [Further Reading / Useful Links](#further-reading--useful-links)
- [Demo](#demo)


### Installation
To install the frontend please do the following:

1. Make sure you have installed [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).
2. Clone this repo with `https://gitlab.com/aossie/Agora-Web`
  - **Note:** *If you just want to use the project, cloning is the best option. However, if you wish to contribute to the project, you will need to fork the project first, and then clone your `Agora-Web` fork and make your contributions via a branch on your fork.*
3. Install and configure [MongoDB](https://www.mongodb.com/)
4. Agora web is using Outh2 for the authentication to run the project.For authentication we are using [Shilhouette](https://www.silhouette.rocks/) library. Before run the application we have to configure the `Shilhouette.conf`.
    1. Make a copy of the `Shilhouette.conf` and rename it to `ShilhouetteLocal.conf`.
    2. Create new applications in [Facebook](https://developers.facebook.com/), [Twitter](https://dev.twitter.com/) and [Google](https://console.cloud.google.com/)
    3. Fill the following values in `ShilhouetteLocal.conf` by the relevant values from your applications.

        ```
        facebook.clientID=${?FACEBOOK_CLIENT_ID}
        facebook.clientSecret=${?FACEBOOK_CLIENT_SECRET}
        google.clientID = ${?GOOGLE_CLIENT_ID}
        google.clientSecret = ${?GOOGLE_CLIENT_SECRET}
        twitter.consumerKey=${?TWITTER_CONSUMER_KEY}
        twitter.consumerSecret=${?TWITTER_CONSUMER_SECRET}

    4. Change the redirect URL in `ShilhouetteLocal.conf` to your localhost `localhost:9000`.
    5. include the `ShilhouetteLocal.conf` into the `Shilhouette.conf`.

        ```
        include "silhouetteLocal.conf"
5. Same as above make a copy of `application.conf` and rename it to `applicationLocal.conf`.Change the following variable as your MOngoDB URI and include the
`applicationLocal.conf` into the `application.conf`.

    ```
    mongodb.default.uri = ${?MONGODB_URI}

6. Add `applicationLocal.conf` , `ShilhouetteLocal.conf` to the `.gitignore` file.

    ```
    /conf/applicationLocal.conf
    /conf/silhouetteLocal.conf


### Running the application
To start the frontend please do the following:

- Start the server by running `sbt run` in the repo folder.
- Go to [http://localhost:9000/](http://localhost:9000/) in a browser. Use your one of your social account to login to the system.
    - **Note:** *Changing any source code while the server is running willautomatically recompile and hot-reload the application on the next HTTP request.*

### Troubleshooting your local environment

Always make sure to `git pull` and get the latest from master. [Google](https://www.google.com) and [Stackoverflow](https://stackoverflow.com/) are your friends. You can find answer for almost all type of technical problems. But if you run into problems you can't resolve,feel free to open an issue.

### Running Tests

To run the test suite locally while developing, just run `sbt test` from the project root.

Tests will also run automatically via Gitlab CI when you push a branch to the repository or a pull request. You can view output by going to the Travis test status from the Pull Request merge box. For this You have to configure the `.gitlab-ci.yml` and add your `HEROKU-API-KEY` as a secret variable in the Gitlab.


## Further Reading / Useful Links

* [sbt](http://www.scala-sbt.org/)
* [Play framework](https://www.playframework.com/)
* [Scala](https://www.scala-lang.org/)
* [Silhouette Documentation](https://www.silhouette.rocks/docs)
* [Casbah MongoDB](https://mongodb.github.io/casbah/)
* [Salat](https://github.com/salat/salat)
* [specs2](https://github.com/etorreborre/specs2)


## Demo

Do you want to see it in action? Here is a working version deployed to heroku

1. Development Version :
https://fathomless-taiga-85734.herokuapp.com/

2. Production Version:
https://agora-web-aossie.herokuapp.com
