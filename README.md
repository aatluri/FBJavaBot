# FBJavaBot
This is a framework which will help you get started with a facebook bot in a few minutes. It consists of boiler plate code that will help you get your bot live very quickly. This was inspired by _([JBot](https://github.com/rampatra/jbot))_.

## Features
1. Consists of the boiler plate code that takes care of providing a public endpoint to integrate with facebook
2. Consists of the boiler plate code which will show you how to respond to facebook using text, buttons, quicl replies etc...
3. Includes verification of the incoming request using hashing to ensure the request is coming from facebook
4. Includes a feature where the application immedietely responds to facebook with an OK event received and then actually processes the event asynchronously. This will avoid situations where facebook sends the same event multiple times thereby causing the bot to send duplicate messages back to facebook.
5. Includes Detailed logging which will help you troubleshoot

## Steps to Get your Facebook Bot running
**Create a working webhook Endpoint**
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


**Create Facebook Page** If you already have a facebook page, you can skip this step. If not, Create a facebook page. https://www.facebook.com/pages/creation/ . If you are doing this just for testing, you can skip most of the setup steps in the link above.
**Create Facebook App**If you already have a facebook app, you can skip this step. You will need to be an admin on this app. If not, go to https://developers.facebook.com/quickstarts/?platform=web. You can “Skip and Create App ID” at the top right. Then create a new Facebook App for your bot and give your app a name, category and contact email. You’ll see your new App ID at the top right on the next page. Scroll down and click “Get Started” next to Messenger.
