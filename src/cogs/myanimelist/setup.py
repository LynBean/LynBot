
from typing import List
from aiohttp import ClientSession

from discord import Embed, Message
from discord.app_commands import Choice
from discord.ext.commands import Bot, hybrid_group, Context
import discord

from src.log import logger
from src.utils import *

from .app import Jikan, Mal


class MalCog(discord.ext.commands.Cog):
    def __init__(self, bot: Bot):
        self.bot = bot
        self.mal = Mal()
        self.jikan = Jikan()

    @hybrid_group(
        name="mal",
        description="MyAnimeList!",
        invoke_without_command=True,
    )
    async def mal(self, _):
        pass

    @mal.command(
        name="anime",
        description="Search for an anime!",
    )
    async def anime(self,
                    context: Context,
                    query: str,
                    ephemeral: bool=False,
                    ):
        logger.info(f"{self.__class__.__name__}: {context.author.display_name} in {context.guild.name}")
        message: Message = await context.reply(
            embed=defer_embed(),
            ephemeral=ephemeral
        )

        async with ClientSession() as session:
            if (await session.get(f"{self.mal.WEB_URL}anime/{query}")).status >= 400:
                await session.close()
                return await message.edit(
                    embed=Embed(
                        title="Please try again!",
                    )
                )

        response = await self.jikan.anime(id=int(query))

        overview = embed_maker(
            title=response["title"],
            description=response["synopsis"],
            url=response["url"],
            Ranking=f"🏆 {response['rank']}",
            Popularity=f"🔥 {response['popularity']}",
            Members=f"😍 {response['members']:,}",
            Type=f"📺 {response['type']}",
            Episodes=f"🆕 {response['releases']}",
            Aired=f"📅 {response['aired_string']}",
        ).set_author(
            name=context.author.display_name,
            icon_url=context.author.avatar.url,
        ).set_footer(
            text=response["background"],
            icon_url="https://image.myanimelist.net/ui/OK6W_koKDTOqqqLDbIoPAiC8a86sHufn_jOI-JGtoCQ",
        ).set_thumbnail(
            url=response["image_url"],
        )

        await message.edit(embed=overview)


    @anime.autocomplete(
        name="query"
    )
    async def anime_autocomplete(self, context: Context, current: str) -> List[Choice]:
        if current.strip() == "":
            return [Choice(name="Keyword must have at least 1 character to search.", value="400")]

        return [
            Choice(name=name, value=mal_id)
            for name, mal_id in await self.mal.search(current, "anime")
        ]



async def setup(bot: Bot):
    await bot.add_cog(MalCog(bot))
    logger.success("Extension 'src.cogs.myanimelist.setup' successfully loaded.")
