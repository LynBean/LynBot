
from aiohttp import ClientSession
from jikanpy import APIException
from typing import List

from discord import Embed, Message, Interaction, SelectOption
from discord.app_commands import Choice, describe, autocomplete
from discord.ext.commands import Bot, hybrid_group, Context
from discord.ui import select, Select
import discord

from src.log import logger
from src.utils import *

from .app import Mal, Anime, Manga


class MalCog(discord.ext.commands.Cog):
    MAL_ICON_URL = "https://image.myanimelist.net/ui/OK6W_koKDTOqqqLDbIoPAiC8a86sHufn_jOI-JGtoCQ"

    def __init__(self, bot: Bot):
        self.bot = bot
        self.mal = Mal()

    @hybrid_group(
        name="mal",
        description="MyAnimeList!",
        invoke_without_command=True,
    )
    async def mal(self, _):
        pass

    async def _prompt_autocomplete(
        self,
        interaction: Interaction,
        current: str
    ) -> List[Choice]:
        """Autocomplete for the title option.
        By sending a request to the MAL API, we can get a list of anime/manga
        """
        return [
            Choice(name=name, value=mal_id)
            for name, mal_id in await self.mal.search(
                current,
                interaction.data["options"][0]["name"],
            )
        ]

    @mal.command(
        name="anime",
        description="Search for an anime!",
    )
    @autocomplete(
        title=_prompt_autocomplete,
    )
    @describe(
        title="The anime you want to search for.",
        ephemeral="Whether or not the response should be ephemeral.",
    )
    async def anime(
        self,
        context: Context,
        title: str,
        ephemeral: bool=False,
    ):
        logger.info(f"{self.__class__.__name__}: {context.author.display_name} in {context.guild.name}")
        message: Message = await context.reply(
            embed=defer_embed(),
            ephemeral=ephemeral
        )

        try:
            modal = Anime(context, int(title))
            await message.edit(embed=await modal.overview(), view=modal)
            await modal.wait()
        except ValueError:
            return await message.edit(
                embed=Embed(
                    title="Looks like you didn't choose a valid anime!",
                    description="Please try again.",
                )
            )
        except APIException as err:
            return await message.edit(
                embed=Embed(
                    title=err.error_json["type"],
                    description=err.error_json["message"],
                ).set_footer(
                    text=err.error_json["error"],
                    icon_url=self.MAL_ICON_URL,
                )
            )

    @mal.command(
        name="manga",
        description="Search for an manga!",
    )
    @autocomplete(
        title=_prompt_autocomplete,
    )
    @describe(
        title="The manga you want to search for.",
        ephemeral="Whether or not the response should be ephemeral.",
    )
    async def manga(
        self,
        context: Context,
        title: str,
        ephemeral: bool=False,
    ):
        logger.info(f"{self.__class__.__name__}: {context.author.display_name} in {context.guild.name}")
        message: Message = await context.reply(
            embed=defer_embed(),
            ephemeral=ephemeral
        )

        try:
            modal = Manga(context, int(title))
            await message.edit(embed=await modal.overview(), view=modal)
            await modal.wait()
        except ValueError:
            return await message.edit(
                embed=Embed(
                    title="Looks like you didn't choose a valid manga!",
                    description="Please try again.",
                )
            )
        except APIException as err:
            return await message.edit(
                embed=Embed(
                    title=err.error_json["type"],
                    description=err.error_json["message"],
                ).set_footer(
                    text=err.error_json["error"],
                    icon_url=self.MAL_ICON_URL,
                )
            )


async def setup(bot: Bot):
    await bot.add_cog(MalCog(bot))
    logger.success("Extension 'src.cogs.myanimelist.setup' successfully loaded.")
