
from typing import List
from aiohttp import ClientSession

from discord import Embed, Message, Interaction, SelectOption
from discord.app_commands import Choice, describe
from discord.ext.commands import Bot, hybrid_group, Context
from discord.ui import select, Select
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
    @describe(
        title="The anime you want to search for.",
        ephemeral="Whether or not the response should be ephemeral.",
    )
    async def anime(self,
                    context: Context,
                    title: str,
                    ephemeral: bool=False,
                    ):
        logger.info(f"{self.__class__.__name__}: {context.author.display_name} in {context.guild.name}")
        message: Message = await context.reply(
            embed=defer_embed(),
            ephemeral=ephemeral
        )

        async with ClientSession() as session:
            if (await session.get(f"{self.mal.WEB_URL}anime/{title}")).status >= 400:
                await session.close()
                return await message.edit(
                    embed=Embed(
                        title="Please try again!",
                    )
                )

        response = await self.jikan.anime(id=int(title))

        class anime_modal(discord.ui.View):
            def __init__(self):
                super().__init__(timeout=300)
                self.overview: Embed = None
                self.fullview: Embed = None

                self._init_overview()
                self._init_fullview()

            def _init_overview(self) -> None:
                if self.overview is not None:
                    return

                self.overview = embed_maker(
                    title=response["title"],
                    description=response["synopsis"],
                    url=response["url"],
                    Score=f"⭐ {response['score']}",
                    Ranking=f"🏆 #{response['rank']}",
                    Popularity=f"🔥 #{response['popularity']}",
                    Members=f"😍 {response['members']:,}",
                    Type=f"📺 {response['type']}",
                    Source=f"📖 {response['source']}",
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

            def _init_fullview(self) -> None:
                if self.fullview is not None:
                    return

                self._init_overview()
                self.fullview = embed_maker(
                    init_embed=self.overview,
                    title=response["title_jp"] or response["title"],
                    Rating=f"🔞 {response['rating']}",
                    Broadcast=f"📺 {response['broadcast']}",
                    ID=response["id"],
                    Scored_by=f"{response['scored_by']:,}",
                    Favorites=f"{response['favorites']:,}",
                    Season=response["season"],
                    Year=response["year"],
                    Producers=response["producers"],
                    Licensors=response["licensors"],
                    Studios=response["studios"],
                    Genres=response["genres"],
                    Themes=response["themes"],
                    Duration=response["duration"],
                ).set_image(
                    url=response["image_url"],
                )

            @select(
                placeholder="Category",
                options=[
                    SelectOption(label="Overview", value="overview"),
                    SelectOption(label="Full View", value="fullview"),
                ]
            )
            async def select_callback(self, interaction: Interaction, select: Select):
                if select.values[0] == "fullview":
                    await interaction.response.edit_message(embed=self.fullview, view=self)
                else:
                    await interaction.response.edit_message(embed=self.overview, view=self)

        view = anime_modal()
        await message.edit(embed=view.overview, view=view)
        await view.wait()


    @anime.autocomplete(
        name="title"
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
