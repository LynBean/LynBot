
DEFAULT_CONFIG = {
    "discord.token": "Get this from https://discord.com/developers/applications",
    "openai.key": "Get this from https://beta.openai.com/account/api-keys",
    "owner.id": "Your Discord ID",
    "admin.ids": ["Discord ID-1", "Discord ID-2"],
}

GPT_DEFAULT_CONFIG = {
    "completion": {
        "echo": False,
        "frequency_penalty": 0,
        "max_tokens": 1024,
        "presence_penalty": 0,
        "temperature": 0.9,
        "top_p": 1,
        "engine": "text-davinci-003",
        "engines": [
            "ada",
            "babbage",
            "code-cushman-001",
            "code-davinci-002",
            "curie-instruct-beta" ,
            "curie",
            "davinci-instruct-beta",
            "davinci",
            "text-ada-001",
            "text-babbage-001",
            "text-curie-001",
            "text-davinci-001",
            "text-davinci-002",
            "text-davinci-003"
        ]
    }
}