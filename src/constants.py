
# Constants

USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36"

## Config

DEFAULT_CONFIG = {
    "discord.token": "Get this from https://discord.com/developers/applications",
    "openai.key": "Get this from https://beta.openai.com/account/api-keys",
    # "myanimelist.token": "Get this from https://myanimelist.net/apiconfig",
    "owner.id": "Your Discord ID",
    "admin.ids": ["Discord ID-1", "Discord ID-2"],
}

GPT_DEFAULT_CONFIG = {
    "completion": {
        "role": "assistant",
        "frequency_penalty": 0,
        "max_tokens": 1024,
        "presence_penalty": 0,
        "temperature": 0.9,
        "top_p": 1,
        "engine": "gpt-3.5-turbo",
        "engines": [
            "code-davinci-002",
            "gpt-3.5-turbo-0301",
            "gpt-3.5-turbo",
            "text-davinci-002",
            "text-davinci-003",
        ],
        "roles": [
            "assistant",
            "system",
            "user",
        ]
    }
}