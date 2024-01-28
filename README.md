SKEL: Skeleton Project

This project provides a starting point for development of projects during the
course "Software Architecture". It is a simple web application offering nearly 
no "real" functionality. Its main purpose is to help you getting started quickly 
by providing a suitable starting point.

It utilizes Spring Boot and is configured as a Maven web application project with:
 - all relevant Spring Framework features enabled
 - embedded Tomcat with support for JSF2
 - embedded H2 in-memory database (including H2 console)
 - support for PrimeFaces
 - basic functionality for user management and Spring web security

Atop of the above functionality, this project was adapted to fit with the 
assignment of the software architecture course in WS23. Hence, it shows how to: 
- make basic rest calls
- parameterize rest calls
- (de)-serialize answer objects into POJOs to be used later

In order to be able to make API calls ensure to provide a valid API-KEY
for https://api.openweathermap.org in your enviroment. 
Software architecture students will
receive their API key from their respective instructors. 
```bash
export SWA_API_KEY=<api-key>
```
Alternatively you can use `.env`-files. In all cases, ensure that your 
confidential API key never leaves your device unencrypted.

For the email service to work, you need an email token that you can connect to 
our email server with the email address group8weatherapp@gmail.com:

```bash
export SWA_EMAIL_PASSWORD=<email-token>
```
We send you the actual token via email.

This project works with Java 17.
Execute  "mvn spring-boot:run" to start the skeleton project and connect to
http://localhost:8080/ to access the skeleton web application. You may login
with "admin" and "passwd".

Feel free to use this skeleton project as you see fit - but keep in mind that
this project is primarily provided to be used for educational purposes. Don't
use it for production.


Contributors:
Christian Sillaber
Michael Brunner
Clemens Sauerwein
Andrea Mussmann
Alexander Blaas
