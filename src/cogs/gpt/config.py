
from src.constants import *
import src

class GPTConfig(src.config.Config):
    def __init__(self):
        super().__init__("gpt_config.json")

    @property
    def default_config(self) -> dict:
        return GPT_DEFAULT_CONFIG

    async def config(self) -> dict:
        """Deprecated: use `raw_config` instead.
        """
        return self.raw_config
