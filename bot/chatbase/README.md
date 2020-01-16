# Chatbase integration

## Apply for a chatbase account

https://chatbase.com/

## Add a bot

https://chatbase.com/bots/main-page

## Configure your BOT with your apikey

Include "tock-analytics-chatbase" in your project

    <dependency>
        <groupId>ai.tock</groupId>
        <artifactId>tock-analytics-chatbase</artifactId>
        <version>${tock}</version>
    </dependency>


Enable chatbase tracking in your bot 

    BotRepository.enableChatbase("API_KEY", "YOUR_VERSION")