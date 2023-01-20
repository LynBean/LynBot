
from aiohttp import ClientSession
from json import load, dump
from os import PathLike
from os import walk
from os.path import join
from pathlib import Path
from typing import Literal, Union
import sys

from .constants import *
from .log import logger
from .utils import *


class Config:
    def __init__(self, filename: str=MISSING):
        self.filename = filename if filename else "config.json"
        self.path: PathLike[str] = join(app_path(), "config", self.filename)
        touch_file(self.filename, "config")

        self._init_config()

    @property
    def default_config(self) -> dict:
        return DEFAULT_CONFIG

    def _init_config(self) -> None:
        """Pre-initializes the config file
        """
        if os.stat(self.path).st_size == 0:
            with open(self.path, "w", encoding="utf-8") as f:
                dump(self.default_config, f, indent=4)

        else:
            self.merge_config(self.path)

    def merge_config(self, config_path: PathLike[str]) -> None:
        # Load the old config
        with open(config_path, "r") as f:
            old_config = load(f)

        # Merge the two configs
        config = self.update_config(old_config, self.default_config)

        # Save the config to the user's config file
        with open(config_path, "w") as f:
            dump(config, f, indent=4)

    def update_config(self, old_config: dict, new_config: dict) -> dict:
        """Recursively updates the old config with the new config.
        """
        for key, value in new_config.items():
            if isinstance(value, dict):
                # If value is a nested dictionary, recursively update it
                node = old_config.setdefault(key, {})
                self.update_config(node, value)
            else:
                # Otherwise, update the key-value pair
                old_config.setdefault(key, value)

        return old_config

    @property
    def raw_config(self) -> dict:
        """Returns the raw config file.
        """
        with open(self.path, "r", encoding="utf-8") as f:
            __config = load(f)

        if self.is_docker():
            __config["discord.token"] = os.environ.get("DISCORD_TOKEN", __config.get("discord.token", ""))
            __config["openai.key"] = os.environ.get("OPENAI_KEY", __config.get("openai.key", ""))
            __config["myanimelist.token"] = os.environ.get("MYANIMELIST_TOKEN", __config.get("myanimelist.token", ""))

        return __config


    async def config(self) -> dict:
        """Returns a config file with validated tokens.
        """
        __config = self.raw_config

        if self.is_docker():
            __config["discord.token"] = os.environ.get("DISCORD_TOKEN", __config["discord.token"])
            __config["openai.key"] = os.environ.get("OPENAI_KEY", __config["openai.key"])
            __config["myanimelist.token"] = os.environ.get("MYANIMELIST_TOKEN", __config.get("myanimelist.token", ""))

        if not await self.validate("openai", __config["openai.key"]):
            __config["openai.key"] = None

        return __config

    def is_docker(self) -> bool:
        """Checks if the bot is running in a Docker container.
        """
        try:
            with open("/proc/self/cgroup", "rt") as f:
                return "docker" in f.read()
        except FileNotFoundError:
            return False

    async def validate(self, validator: Literal["discord", "openai"], token: Union[str, int]) -> bool:
        """Validates the token.
        """
        if validator == "discord":
            url = "https://discord.com/api/v10/users/@me"
            header_auth = f"Bot {token}"

        elif validator == "openai":
            url = "https://api.openai.com/v1/models"
            header_auth = f"Bearer {token}"

        async with ClientSession() as session:
            async with session.get(url, headers={"Authorization": header_auth}) as r:
                response = r

        if response.status in (200, 304):
            return True
        else:
            return False
