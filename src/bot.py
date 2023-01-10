
from os import walk
from os.path import join
from pathlib import Path
import argparse
import asyncio
import openai
import os
import platform
import sys

from discord import Intents, Message, Interaction
from discord.ext.commands import Context
import discord

from .config import Config
from .log import logger
from .utils import app_path, version, MISSING

class Yor(discord.ext.commands.AutoShardedBot):
    def __init__(self):
        super().__init__(
            command_prefix="",
            intents=Intents.all(),
            description="",
        )

        self._config = Config()

    @property
    def raw_config(self) -> dict:
        return self._config.raw_config

    async def config(self) -> dict:
        return await self._config.config()

    async def setup_cogs(self) -> None:
        path = join(Path(__file__).parent.resolve(), "cogs")

        for dirpath, _, filenames in walk(path):
            if len(filenames) == 0:
                continue

            dirpath = dirpath.replace("\\", ".").replace("/", ".")
            dirpath = dirpath[dirpath.find("src"):]

            for filename in filenames:
                if not filename.endswith(".py"):
                    continue

                filepath = f"{dirpath}.{filename[:-3]}"

                try:
                    await self.load_extension(name=filepath)
                except Exception as err:
                    logger.debug(err)
                    continue

    async def setup_hook(self) -> None:
        await self.setup_cogs()

    async def on_ready(self) -> None:
        await self.tree.sync()

        logger.info(f"Logged in as {self.user} (ID: {self.user.id})")
        logger.info(f"Connected to {len(self.guilds)} guilds")
        logger.info(f"Ver. Bot: {version()}")
        logger.info(f"Ver. OpenAI: {openai.version.VERSION}")
        logger.info(f"Ver. Discord: {discord.__version__}")
        logger.info(f"Ver. Python: {sys.version}")
        logger.info(f"Running on: {platform.system()} {platform.release()} ({os.name})")

        self.app_info = await self.application_info()

    async def on_message(self, message: Message) -> None:
        pass

    async def on_command_error(self, context: Context, exception: Exception) -> None:
        await context.reply(f"{context.author.mention} {exception}")
        logger.error(exception)

def _main():
    bot = Yor()

    @bot.tree.command(
        name="ping",
        description="Pong!",
    )
    async def ping(interaction: Interaction):
        await interaction.response.send_message(f"Pong! {bot.latency * 1000:.2f}ms")

    try:
        bot.run(
            bot.raw_config["discord.token"],
            reconnect=True,
            log_level=20 # CRITICAL 50, ERROR 40, WARNING 30, INFO 20, DEBUG 10, NOTSET 0
        )
    except discord.errors.LoginFailure as err:
        logger.critical(f"{err} Please check your token in the config file in the following path: {bot._config.path}")
        asyncio.run(asyncio.sleep(3600))

# @logger.catch
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("-v", "--version", action="store_true")
    args = parser.parse_args()

    if args.version:
        print(app_path())
        print(version())
        sys.exit(0)

    _main()
