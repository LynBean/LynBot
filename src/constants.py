
# Constants

USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36"

## Config

DEFAULT_CONFIG = {
    "discord.token": "MTA3MjM5MzYzOTM4NjQyMzM2MA.GLbuMo.tocl-2zFyrMVVakPuUquoxDrwAB01QxF69970Y",
    "openai.key": "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",
    # "myanimelist.token": "Get this from https://myanimelist.net/apiconfig",
    "owner.id": "1068054172123013130",
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