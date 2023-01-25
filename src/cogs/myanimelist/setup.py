
from aiohttp import ClientSession
from jikanpy import APIException
from typing import List, Literal

from discord import Embed, Message, Interaction, SelectOption
from discord.app_commands import Choice, describe, autocomplete
from discord.ext.commands import Bot, hybrid_group, Context
from discord.ui import select, Select
import discord

from src.log import logger
from src.utils import *

from .app import Mal, Jikan, Anime, Manga, Seasonal, Top, Random


class MalCog(discord.ext.commands.Cog):
    MAL_ICON_URL = "https://image.myanimelist.net/ui/OK6W_koKDTOqqqLDbIoPAiC8a86sHufn_jOI-JGtoCQ"

    def __init__(self, bot: Bot):
        self.bot = bot
        self.mal = Mal()
        self.jikan = Jikan()

    # ---------------------------- #
    # Base group for MAL commands  #

    @hybrid_group(
        name="mal",
        description="MyAnimeList!",
        invoke_without_command=True,
    )
    async def mal(self, _):
        pass

    # ---------------------------- #
    # Autocomplete for anime/manga #

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

    # ---------------------------- #
    #     Anime/Manga Commands    #

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

    # ---------------------------- #
    #      Seasonal Anime          #

    @mal.command(
        name="season-now",
        description="Get the current season's anime!",
    )
    async def season_now(
        self,
        context: Context,
        ephemeral: bool=False,
    ):
        message: Message = await context.reply(
            embed=defer_embed(), ephemeral=ephemeral
        )

        seasonal = Seasonal(context)
        await seasonal.init_now()
        seasonal.reload()
        await message.edit(
            embed=seasonal.entry_embed(0),
            view=seasonal
        )
        await seasonal.wait()
        await message.edit(view=None)

    @mal.command(
        name="season-upcoming",
        description="Get the upcoming season's anime!",
    )
    async def season_upcoming(
        self,
        context: Context,
        filter: Literal[
            "tv", "movie", "ova", "special", "ona", "music"
        ]=None,
        ephemeral: bool=False,
    ):
        message: Message = await context.reply(
            embed=defer_embed(), ephemeral=ephemeral
        )

        seasonal = Seasonal(context)
        await seasonal.init_upcoming(filter=filter)
        seasonal.reload()
        await message.edit(
            embed=seasonal.entry_embed(0),
            view=seasonal
        )
        await seasonal.wait()
        await message.edit(view=None)

    @mal.command(
        name="season-past",
        description="Get the past season's anime!",
    )
    async def season_past(
        self,
        context: Context,
        year: int,
        season: str,
        ephemeral: bool=False,
    ):
        message: Message = await context.reply(
            embed=defer_embed(), ephemeral=ephemeral
        )

        seasonal = Seasonal(context)
        await seasonal.init_season(int(year), season)
        seasonal.reload()
        await message.edit(
            embed=seasonal.entry_embed(0),
            view=seasonal
        )
        await seasonal.wait()
        await message.edit(view=None)

    @season_past.autocomplete(
        name="year",
    )
    async def year_autocomplete(
        self,
        interaction: Interaction,
        current: str
    ) -> List[Choice]:
        response = await self.jikan.seasons()

        return [
            Choice(name=f"Year {data['year']}", value=data["year"])
            for index, data in enumerate(response["data"])
            if index < 25
        ]

    @season_past.autocomplete(
        name="season",
    )
    async def season_autocomplete(
        self,
        interaction: Interaction,
        current: str
    ) -> List[Choice]:
        for option in interaction.data["options"][0]["options"]:
            if option["name"] == "year":
                year = option["value"]
                break
        else:
            year = None

        if year is None:
            return [
                Choice(name=var, value=var)
                for var in ("spring", "summer", "fall", "winter")
            ]

        response = await self.jikan.seasons()
        for data in response["data"]:
            if data["year"] == year:
                return [
                    Choice(name=var, value=var)
                    for var in data["seasons"]
                ]

    # ---------------------------- #
    #     Top Anime/Manga          #

    @mal.command(
        name="top-anime",
        description="Get the top anime!",
    )
    async def top_anime(
        self,
        context: Context,
        type: Literal[
            "tv", "movie", "ova", "special", "ona", "music"
        ]=None,
        filter: Literal[
            "airing", "upcoming", "bypopularity", "favorite"
        ]=None,
        ephemeral: bool=False,
    ):
        message: Message = await context.reply(
            embed=defer_embed(), ephemeral=ephemeral
        )

        top = Top(context)
        await top.init_anime(entry_type=type, filter=filter)
        top.reload()
        await message.edit(
            embed=top.entry_embed(0),
            view=top
        )
        await top.wait()
        await message.edit(view=None)

    @mal.command(
        name="top-manga",
        description="Get the top manga!",
    )
    async def top_manga(
        self,
        context: Context,
        entry_type: Literal[
            "manga", "novel", "lightnovel", "oneshot", "doujin", "manhwa", "manhua"
        ]=None,
        filter: Literal[
            "publishing", "upcoming", "bypopularity", "favorite"
        ]=None,
        ephemeral: bool=False,
    ):
        message: Message = await context.reply(
            embed=defer_embed(), ephemeral=ephemeral
        )

        top = Top(context)
        await top.init_manga(entry_type=type, filter=filter)
        top.reload()
        await message.edit(
            embed=top.entry_embed(0),
            view=top
        )
        await top.wait()
        await message.edit(view=None)

    # ---------------------------- #
    #    Random Anime/Manga        #

    @mal.command(
        name="random-anime",
        description="Get a random anime!",
    )
    async def random_anime(
        self,
        context: Context,
        how_many: int=3,
        ephemeral: bool=False,
    ):
        message: Message = await context.reply(
            embed=defer_embed(), ephemeral=ephemeral
        )

        random = Random(context, number=how_many)
        await random.init_anime()
        random.reload()
        await message.edit(
            embed=random.entry_embed(0),
            view=random
        )
        await random.wait()
        await message.edit(view=None)

    @mal.command(
        name="random-manga",
        description="Get a random manga!",
    )
    async def random_manga(
        self,
        context: Context,
        how_many: int=3,
        ephemeral: bool=False,
    ):
        message: Message = await context.reply(
            embed=defer_embed(), ephemeral=ephemeral
        )

        random = Random(context, number=how_many)
        await random.init_manga()
        random.reload()
        await message.edit(
            embed=random.entry_embed(0),
            view=random
        )
        await random.wait()
        await message.edit(view=None)


async def setup(bot: Bot):
    await bot.add_cog(MalCog(bot))
    logger.success("Extension 'src.cogs.myanimelist.setup' successfully loaded.")
