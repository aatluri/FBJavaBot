# FBJavaBot
This is a framework which will help you get started with a facebook bot in a few minutes. It consists of boiler plate code that will help you get your bot live very quickly. This was inspired by _([JBot](https://github.com/rampatra/jbot))_.

## Features
1. Consists of the boiler plate code that takes care of providing a public endpoint to integrate with facebook
2. Consists of the boiler plate code which will show you how to respond to facebook using text, buttons, quicl replies etc...
3. Includes verification of the incoming request using hashing to ensure the request is coming from facebook
4. Includes a feature where the application immedietely responds to facebook with an OK event received and then actually processes the event asynchronously. This will avoid situations where facebook sends the same event multiple times thereby causing the bot to send duplicate messages back to facebook.
5. Includes Detailed logging which will help you troubleshoot

## Steps to Get your facebook Bot running
1. If you already have a facebook page, you can skip this step. If not, Create a facebook page. https://www.facebook.com/pages/creation/ . If you are doing this just for testing, you can skip most of the setup steps in the link above.
2. If you already have a facebook app, you can skip this step. You will need to be an admin on this app. If not, go to https://developers.facebook.com/quickstarts/?platform=web. 
You can “Skip and Create App ID” at the top right. Then create a new Facebook App for your bot and give your app a name, category and contact email.
You’ll see your new App ID at the top right on the next page. Scroll down and click “Get Started” next to Messenger.
