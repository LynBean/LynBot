
from discord import Message, Embed, SelectOption, Interaction
from discord.ext.commands import Bot, hybrid_command, Context
from discord.ui import select, Select
import discord

from src.log import logger

from .app import App


class YorEpicWeeklyFree(discord.ext.commands.Cog):
    def __init__(self, bot: Bot):
        self.bot = bot

    @hybrid_command(
        name="epicweeklyfree",
        description="Get the weekly free games from Epic Games.",
    )
    async def weeklyfree(self, context: Context):
        logger.info(f"{self.__class__.__name__}: {context.author.display_name} in {context.guild.name}")

        message: Message = await context.reply(
            embed=Embed(title="Chotto Matte Kudasai")
        )

        app = App()
        await app.start()
        data = app.games.to_dict()["data"]
        data.sort(
            key=lambda x: x["ongoing"],
            reverse=True
        )

        def embed(data: dict) -> Embed:
            return Embed(
                title=data["title"],
                description=data["description"],
                url="https://store.epicgames.com/en-US/free-games"
            ).add_field(
                name="Original Price",
                value=data["pricing"],
            ).add_field(
                name="Seller",
                value=data["seller"],
            ).set_author(
                name=data["fmt_promo_date"]
            ).set_image(
                url=data["image_url"]
            )

        class epicview(discord.ui.View):
            def __init__(self):
                super().__init__(timeout=300)

            @select(
                placeholder="All the weekly free games from Epic Games.",
                options=[
                    SelectOption(
                        label=f"{'🆕 FREE NOW!' if data[index]['ongoing'] else '🆙 Upcoming!'} {data[index]['title']}",
                        value=index
                    )
                    for index in range(len(app.games))
                ],
            )
            async def callback(self, interaction: Interaction, select: Select):
                await interaction.response.defer()
                await interaction.followup.edit_message(
                    message_id=message.id,
                    embed=embed(data[int(interaction.data["values"][0])]),
                    view=self,
                )

        view = epicview()
        await message.edit(embed=embed(data[0]), view=view)
        await view.wait()
        await message.edit(view=None)


async def setup(bot: Bot):
    await bot.add_cog(YorEpicWeeklyFree(bot))
    logger.success("Extension 'src.cogs.epicgames.setup' successfully loaded.")
