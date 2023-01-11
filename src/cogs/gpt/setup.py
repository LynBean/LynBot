
from typing import Literal, List, Union
import openai

from discord import Interaction, Embed, Message
from discord.ext.commands import hybrid_command, Bot, Context
from discord.app_commands import describe
from discord.utils import MISSING
import discord

from src.log import logger

from .app import Completion
from .config import GPTConfig


class YorGPT(discord.ext.commands.Cog):
    def __init__(self, bot: Bot):
        self.bot = bot
        self._config = GPTConfig()

    @property
    def raw_config(self) -> dict:
        return self._config.raw_config

    def embed_trim(self, texts: str, length: int=4096) -> List[Embed]:
        """Trims the text into a list of Embeds.
        """
        if len(texts.strip()) == 0:
            return [Embed(description="^_^")]
        return [
            Embed(description=texts[page * length: (page + 1) * length])
            for page in range( -(-len(texts) // length) )
        ]

    @hybrid_command(
        name="chat",
        description="Chat with the bot.",
    )
    @describe(
        text="I love human.",
        ephemeral="Whether the response should be private or not.",
        engine="The engine to use for the chat.",
        frequency_penalty="Number between -2.0 and 2.0. Positive values penalize new tokens.",
        max_tokens="The maximum number of tokens to generate in the completion.",
        presence_penalty="Number between -2.0 and 2.0. Positive values penalize new tokens.",
        temperature="Try 0.9 for more creative applications, and 0 for ones with a well-defined answer.",
        top_p="An alternative to sampling with temperature, called nucleus sampling.",
        api_key="API Key?",
    )
    async def chat(self,
                   context: Context, *, text,
                   ephemeral: bool=False,
                   engine: Literal[tuple(GPTConfig().raw_config["completion"]["engines"])]=None,
                   frequency_penalty: float=None, max_tokens: int=None, presence_penalty: float=None,
                   temperature: float=None, top_p: float=None, api_key: str=None) -> None:

        try:
            completion = Completion(
                prompt=text, engine=engine, frequency_penalty=frequency_penalty,
                max_tokens=max_tokens, presence_penalty=presence_penalty, temperature=temperature,
                top_p=top_p, user=str(context.author.id), api_key=api_key
            )
        except openai.error.OpenAIError as err:
            await context.reply(
                embed=Embed(
                    title = type(err).__name__,
                    description = err.user_message
                ),
                ephemeral=ephemeral
            )

            return logger.warning(f"Error made by {context.author.display_name} in {context.guild.name}\n{type(err).__name__}: {err.user_message}")

        embed = Embed(
            title = text[:256],
        ).set_author(
            name = context.author.display_name,
            icon_url = context.author.avatar.url,
        ).set_footer(
            text = f"Powered by {completion.engine}",
            icon_url = "https://openai.com/content/images/2022/05/openai-avatar.png"
        )
        await context.reply(
            embed=embed, ephemeral=ephemeral
        )

        message: Message = await context.reply(
            ephemeral=ephemeral,
            embed=Embed(description="!@#$%^&*()_+")
        )

        logger.info(f"{self.__class__.__name__}: {context.author.display_name} in {context.guild.name}\n{repr(completion)}")
        completion.start()

        embeds: List[Embed] = self.embed_trim(completion.text)
        async def send_embeds():
            nonlocal embeds, message
            prev_embeds_count = len(embeds)
            embeds = self.embed_trim(completion.text)

            if prev_embeds_count != len(embeds):
                message = await context.reply(
                    embed=embeds[-1],
                    ephemeral=ephemeral
                )
            else:
                await message.edit(embed=embeds[-1])

        async with context.channel.typing():
            while bool(completion):
                await send_embeds()
            else:
                await send_embeds()


async def setup(bot: Bot):
    __config = await bot.config()
    if __config["openai.key"] is None:
        logger.error(f"Invalid OpenAI key. Please set it in the config file ({bot._config.path})")
        
        if bot._config.is_docker():
            logger.warning(f"Detected Docker environment: Please set the environment variable 'OPENAI_KEY' to your valid key.")
            
        return
    
    await bot.add_cog(YorGPT(bot))
    logger.success("Extension 'src.cogs.gpt.setup' successfully loaded.")
