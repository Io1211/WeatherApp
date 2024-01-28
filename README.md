
**Winter term 2023/24 - PS Softwarearchitektur - Project WeatherApp**

This project is based on the skeleton project provided for the course and implements
a basic weather app. The app allows users to search for different locations and 
check their current, hourly and daily weather. If an user decides to upgrade to 
premium status they obtain more weather statistics and for longer time spans. Additionally
users can add a location to a favorites list for which they can also configure all values.
To manage payment, managers have access to a billing overview, which tracks payments and 
the subscription status of users. Lastly, user data and details can be manipulated by 
an administrator in the admin overview.



**Technical details**

The project utilizes Spring Boot and is configured as a Maven web application project with:
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

Contributors of the skeleton project:
Christian Sillaber
Michael Brunner
Clemens Sauerwein
Andrea Mussmann
Alexander Blaas



**Missing features**

Holiday Planner will not be implemented.



**Preconfigured users**

The following users and roles are added on start-up of the project:
(The password for all users is passwd)

- admin: REGISTERED_USER, ADMIN
- testUser: REGISTERED_USER
- testPremium: REGISTERED_USER, PREMIUM_USER
- testPremiumBad: REGISTERED_USER
- testManager: 'REGISTERED_USER, MANAGER

- admin2: REGISTERED_USER, ADMIN
- user1: REGISTERED_USER, MANAGER
- user2: REGISTERED_USER
- elvis: REGISTERED_USER, ADMIN
- premium1: REGISTERED_USER, PREMIUM_USER



**Running the project**

To run the project these commands can be used:
(Note that the API key AND the email password have to be defined before running.)
- export SWA_API_KEY=<psswd>
- export SWA_EMAIL_PASSWORD=<psswd>
- mvn spring-boot:run
- mvn test
- mvn test -Dtest=<testclass> e.g. mvn test -Dtest=at.qe.skeleton.tests.WeatherBeanTest#formatInstantToDateTimeTest



**Decisions**

Subscription service decisions:

* Subscription data can only be accessed for past months. Data for the current and future month isn't available as it doesn't make sense e.g., to set the status of the month as paid if it is just the start of the month and bill 2 days. To keep accurate track of billed days that way would be needlessly complicated.
* All the users are shown in the billing management page, even those who don't have a subscription. For this type of user, all fields in the view including premium status are set to "-".
* The premium status field always refers to the currently selected month and year and not to whether the subscription is active or not right now. By default (i.e., when first loading the page) the month before the current month is selected and displayed in the page.
* In the detailed billing view:

* The payment confirmation and subscription revoking has been split into two distinct buttons (makes it clearer for the manager)

* Payment status can be set and unset as often as desired but only if the user had an active subscription that month (can't set to paid a month where no days were billed).

* Revoking a subscription removes the premium role from a user and deletes their subscription. This means that all the data about past premium months and payments will also be permanently deleted. The user can activate a new subscription (activate premium) after the old one was terminated by a manager.

* The Payment successful and Revoke subscription buttons are to be understood as flags. If they are set to yes and the save button is pressed, the corresponding action is performed e.g., the subscription is revoked or the payment status is set to paid. These flags are sticky i.e., once set for one user, the flag will appear set also for all other users if their edit detail view is accessed. This was chosen for quicker and simpler development due to time constraints.

* If a payment has been set to paid, the payment status will be set to paid and a new payment will be generated for the user under the hood. Setting the flag to No and pressing the save button will delete the payment and reset the payment status to pending.
If Payment success is set to Yes and saved multiple times, multiple payments will be generated but the status will always show paid. Thus, when revoking the payment, setting Payment success to No will also require an equal amount of pressing the save button before the status will return to pending

* Our current mock data doesn't include emails for users. If a manager terminates a subscription manually, the user is informed via email. This can be tested by logging in as admin, setting the email of a user manually (e.g., to a 10 minute mail), then logging in as manager and terminating the user's subscription.



**Known issues and bugs**

- Searching for "seefeld" gives the option "Gemeinde Seefeld in Tirol, Tyrol, AT". If the latter is chosen an error message will be displayed that the location cannot be found. We suspect an issue with the location API.
- We ran into several Problems with lazy fetching from the DB in both running and testing enviroment. Decided to use fetchtype eager due to time-constraints towards the end.
