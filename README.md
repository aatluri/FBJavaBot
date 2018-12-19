# FBJavaBot
This is a framework which will help you get started with a facebook bot in a few minutes. It consists of boiler plate code that will help you get your bot live very quickly. This was inspired by _([JBot](https://github.com/rampatra/jbot))_.

## Features
1. Consists of the boiler plate code that takes care of providing a public endpoint to integrate with facebook
2. Consists of the boiler plate code which will show you how to respond to facebook using text, buttons, quick replies etc...
3. Includes verification of the incoming request using hashing to ensure the request is coming from facebook
4. Includes a feature where the application immedietely responds to facebook with an OK event received and then actually processes the event asynchronously. This will avoid situations where facebook sends the same event multiple times thereby causing the bot to send duplicate messages back to facebook.
5. Includes Detailed logging which will help you troubleshoot

## Steps to Get your Facebook Bot running
**Create a working public Endpoint**
1. You will need a public endpoint. So for that you can use somethng like ngrok. Go to https://ngrok.com/download and download nrtok. From the command line , navigate to the folder containing ngrok and type ./ngrok http 8080. This will display the public https endpoint you can use.
2. Clone this project https://github.com/aatluri/FBJavaBot.git 
3. Run the project in you local environment by running FBJavaBot in your ide or from the command line:
    ```bash
    $ cd FBJavaBot
    $ mvn clean install
    $ mvn spring-boot:run
    ```
4. Once the application runs successfully, you should be able to hit http://localhost:8080/FBJavaBot/fbwebhook
5. You will see a http 403 Not authorized page. Dont worry this is expected since we called the endopoint without passing the verify token.
6. If you go back to your ide or command line, you should see some logging which tells you that the endpoint was hit.
7. Now if you look at the terminal/command line prompt where you ran ngrok, you should see something like "Forwarding" and a https endpoint like https://<alphanumeric text>.ngrok.io -> localhost:8080. So now your public webhook endpoint is https://<alphanumeric text>.ngrok.io/FBJavaBot/fbwebhook.

**If you already have a facebook page and facebook app, then update the page access token and appSecret in the https://github.com/aatluri/FBJavaBot/blob/master/src/main/resources/application.properties file. You can find the appSecret in the facebook app Settings under Basic Settings**

**Create Facebook Page** 
1. If you already have a facebook page, you can skip this section. You will need to be an admin on the page.
2. If not, Create a facebook page. https://www.facebook.com/pages/creation/ . 
3. If you are doing this just for testing, you can skip most of the setup steps in the link above.

**Create Facebook App**
1. If you already have a facebook app, you can skip this step. You will need to be an admin on this app. 
2. If not, go to https://developers.facebook.com/quickstarts/?platform=web. 
3. You can “Skip and Create App ID” at the top right. Then create a new Facebook App for your bot and give your app a name, category and contact email. 
4. You’ll see your new App ID at the top right on the next page. Scroll down and click “Get Started” next to Messenger.
5. There are a few things you will need to do in the messenger settings of your app to wire up your page to the endpoint you created earlier.
6. Generate a Page Access Token : Under Messenger Settings, Scroll down and under Token generation, Select the page you created above from the drop down. If you are not able to see your page in the drop down, it means you are not an admin on the page. Now copy the token generated to the https://github.com/aatluri/FBJavaBot/blob/master/src/main/resources/application.properties
7. In the Same page, you will see a section called Webhooks. Here Click oin Edit Events and subscribe to messages, messaging_postbacks, messaging_optins, message_echoes
8. Right below this, you will see "Select a page to subscribe your webhook to the page events". Select your facebook page here. If you are not able to see your page, it means you are not an admin on the page.
7. Go to the App Basic Settings page and get the appSecret and copy it to the https://github.com/aatluri/FBJavaBot/blob/master/src/main/resources/application.properties file
8.Go to Webhook and click on Edit Subscription. 
9. Paste https://<alphanumeric text>.ngrok.io/FBJavaBot/fbwebhook in Callback URL
10. Paste the value of the facebook.verifytoken property in the https://github.com/aatluri/FBJavaBot/blob/master/src/main/resources/application.properties file in the Verify Token box.
11. Click on Verify and Save. If all goes right it should just close the pop up and go back to the Webhooks page. If you go back to your IDE or local command line where you arte running the application, you should see some logs which indicate that a call was made to the fbwebhook endpoint with a success.
12. Now your page, app and bot endpoint are all connected.

**Chat with the Facebook Bot**
1. Go back to your facebook page
2. Hover over the "Send Message" button and click on Test Button
3. This will open up a chat window
4. Type something and hit Enter, the bot should respond with the default message configured in the code "Hi There, How are you doing?"

**TODO**
1. Add proper test coverage
2. Ability to configure the same appication to work with multiple facebook pages
3. Provide architecture for the ability to manage state so that when a user comes back to the page, they can start from where they left off previously.
