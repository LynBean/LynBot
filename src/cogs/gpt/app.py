
from threading import Thread
from typing import Iterable
import openai

from src.log import logger
from src.utils import MISSING
import src

from .config import GPTConfig

class OpenAIApp:
    def __init__(self, api_key: str=MISSING):
        self.openai_key = api_key if api_key else src.config.Config().raw_config["openai.key"]
        openai.api_key = self.openai_key
        openai.log = "info"

class Completion(OpenAIApp):
    def __init__(self, *, prompt: str, role: str=MISSING,
                 engine: str=MISSING, frequency_penalty: float=MISSING, max_tokens: int=MISSING,
                 presence_penalty: float=MISSING, temperature: float=MISSING, top_p: float=MISSING,
                 user: str=MISSING,
                 echo: bool=MISSING,
                 api_key: str=MISSING):

        super().__init__(api_key)

        self.gpt_config = GPTConfig()
        self.prompt = prompt
        self.engine = engine if engine else self.gpt_config.raw_config["completion"]["engine"]
        self.frequency_penalty = frequency_penalty if frequency_penalty else self.gpt_config.raw_config["completion"]["frequency_penalty"]
        self.max_tokens = max_tokens if max_tokens else self.gpt_config.raw_config["completion"]["max_tokens"]
        self.presence_penalty = presence_penalty if presence_penalty else self.gpt_config.raw_config["completion"]["presence_penalty"]
        self.temperature = temperature if temperature else self.gpt_config.raw_config["completion"]["temperature"]
        self.top_p = top_p if top_p else self.gpt_config.raw_config["completion"]["top_p"]
        self.role = role if role else self.gpt_config.raw_config["completion"]["role"]
        self.user = user

        self.create = self._create()
        self._thread: Thread = Thread(target=self._iter_create)
        self._text: str = ""

    def __repr__(self):
        return f"<Completion prompt={self.prompt!r} engine={self.engine!r} frequency_penalty={self.frequency_penalty!r} " \
            f"max_tokens={self.max_tokens!r} presence_penalty={self.presence_penalty!r} temperature={self.temperature!r} " \
            f"top_p={self.top_p!r} user={self.user!r} role={self.role!r}>"

    def __bool__(self) -> bool:
        """Returns True if the iterator is running, False otherwise.
        """
        return self._thread.is_alive()

    @property
    def text(self):
        """Returns the response of the completion.
        """
        return self._text

    def _create(self) -> Iterable[dict]:
        return openai.ChatCompletion.create(
            messages=[
                {
                    "role": self.role,
                    "content": self.prompt
                }
            ],
            model=self.engine,
            frequency_penalty=self.frequency_penalty,
            max_tokens=self.max_tokens,
            presence_penalty=self.presence_penalty,
            temperature=self.temperature,
            top_p=self.top_p,
            user=self.user,
            stream=True,
        )

    def _iter_create(self) -> None:
        for response in self.create:
            self._text += response["choices"][0]["delta"].get("content", "")

    def start(self) -> None:
        """Starts the completion.
        """
        self._thread.start()
