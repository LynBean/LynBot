
from aiohttp import ClientSession
from copy import deepcopy
from jikanpy import AioJikan
from jikanpy.exceptions import APIException
from typing import TypeVar, Union, Optional, Dict, Any, Literal, List, Tuple, Callable
import asyncio

from discord import Embed, Interaction, SelectOption, ButtonStyle
from discord.ext.commands import Context
from discord.ui import select, Select, View, button, Button
import discord

from src.config import Config
from src.constants import *
from src.utils import *

from .utils import *


MalT = TypeVar("MalT", bound="Mal")
config = Config()


class Jikan(AioJikan):
    def __init__(self):
        super().__init__()

    async def _request(
        self,
        url: str,
        **kwargs: Union[int, Optional[str]]
    ) -> Dict[str, Any]:
        """Returns the response from the given url.
        Headers are set to accept gzip encoding, no store cache, json content.
        """
        session = await self._get_session()
        response = await session.get(
            url=url,
            headers={
                'Accept-Encoding': 'gzip',
                'Cache-Control': 'no-store',
                'Content-Type': 'application/json',
                'User-Agent': USER_AGENT,
            },
        )
        return await self._wrap_response(response, url, **kwargs)


class Mal:
    API_URL = "https://api.myanimelist.net/v2/"
    WEB_URL = "https://myanimelist.net/"

    def __init__(self):
        self.session: ClientSession = None

    async def __aenter__(self) -> MalT:
        return self

    async def __aexit__(self, *excinfo: Any) -> None:
        await self.close()

    async def close(self) -> None:
        """Close AioHTTP session
        """
        if self.session is not None:
            await self.session.close()

    async def _get_session(self) -> ClientSession:
        """Get AioHTTP session by creating it if it doesn't already exist
        """
        if self.session is None:
            self.session = ClientSession()

        return self.session

    async def _request(
        self,
        url: str,
    ) -> Dict[str, Any]:
        """Makes a request to the Mal API given the url and wraps the response.
        """
        session = await self._get_session()
        response = await session.get(
            url=url,
            headers={
                "X-MAL-ID": config.raw_config["myanimelist.token"],
                "User-Agent": USER_AGENT,
            },
        )
        result = await response.json()
        result["headers"] = dict(response.headers)
        result["status_code"] = response.status
        result["url"] = url
        return result

    async def search(
        self,
        query: str,
        entry_type: Literal[
            "anime", "manga", "character", "person", "user"
        ]
    ) -> List[Tuple[str, str]]:
        """Searches for an anime or manga on Mal.
        """
        response = await self._request(
            f"{self.WEB_URL}search/prefix.json?type={entry_type}&keyword={query}"
        )

        if response["status_code"] >= 400:
            return [(
                response["errors"][0]["message"],
                str(response["status_code"])
            )]

        return [
            (entry["name"], str(entry["id"])) if entry_type != "user"
            else (entry["name"], entry["name"])

            for index, entry in enumerate(response["categories"][0]["items"])
            if index <= 25
        ]


class Entry_Modal(discord.ui.View):
    def __init__(
        self,
        context: Context,
        timeout: Optional[float]=MISSING
    ):
        super().__init__(
            timeout=timeout or config.raw_config["interaction.timeout"]
        )
        self.jikan = Jikan()
        self.context = context
        self._data: Dict[str, Any] = None
        self._overview: Embed = None
        self._fullview: Embed = None
        self._links: List[Dict[str, str]] = None

    async def data(self) -> Dict[str, Any]:
        return self._data

    async def overview(self) -> Embed:
        if self._overview is not None:
            return self._overview

        await self.data()
        self._overview = wrap_overview(
            self._data
        ).set_author(
            name=self.context.author.display_name[:256],
            icon_url=self.context.author.avatar.url
        )
        return self._overview

    async def fullview(self) -> Embed:
        if self._fullview is not None:
            return self._fullview

        await self.data()
        self._fullview = wrap_fullview(
            self._data
        ).set_author(
            name=self.context.author.display_name[:256],
            icon_url=self.context.author.avatar.url
        )
        return self._fullview

    @select(
        placeholder="Category",
        options=[
            SelectOption(label="Overview", value="overview"),
            SelectOption(label="Full View", value="fullview"),
        ]
    )
    async def select_callback(self, interaction: Interaction, _: Select):
        await interaction.response.defer()
        await interaction.followup.edit_message(
            message_id=interaction.message.id,
            embed=await getattr(self, interaction.data["values"][0])(),
            view=self,
        )


class Anime(Entry_Modal):
    def __init__(
        self,
        context: Context,
        id: int,
    ):
        super().__init__(context)
        self.id = id

    async def data(self) -> Dict[str, Any]:
        if self._data is None:
            response = await self.jikan.anime(
                int(self.id),
                extension="full"
            )

            self._data = wrap_data(response)

        return await super().data()


class Manga(Entry_Modal):
    def __init__(
        self,
        context: Context,
        id: int,
    ):
        super().__init__(context)
        self.id = id

    async def data(self) -> Dict[str, Any]:
        if self._data is None:
            response = await self.jikan.manga(
                int(self.id),
                extension="full"
            )

            self._data = wrap_data(response)

        return await super().data()


class Characters(Entry_Modal):
    def __init__(
        self,
        context: Context,
        id: int,
    ):
        super().__init__(context)
        self.id = id

    async def data(self) -> Dict[str, Any]:
        if self._data is None:
            response = await self.jikan.characters(
                int(self.id),
                extension="full"
            )

            self._data = wrap_data(response)

        return await super().data()


class People(Entry_Modal):
    def __init__(
        self,
        context: Context,
        id: int,
    ):
        super().__init__(context)
        self.id = id

    async def data(self) -> Dict[str, Any]:
        if self._data is None:
            response = await self.jikan.people(
                int(self.id),
                extension="full"
            )

            self._data = wrap_data(response)

        return await super().data()

class Users(Entry_Modal):
    def __init__(
        self,
        context: Context,
        username: str,
    ):
        super().__init__(context)
        self.username = username

    async def data(self) -> Dict[str, Any]:
        if self._data is None:
            response = await self.jikan.users(
                self.username,
                extension="full"
            )

            self._data = wrap_data(response)

        return await super().data()


class Entries_Modal(discord.ui.View):
    def __init__(
        self,
        context: Context,
        data,
        timeout: Optional[int]=MISSING,
    ):
        super().__init__(
            timeout=timeout or config.raw_config["interaction.timeout"]
        )
        self.context = context
        self.jikan = Jikan()
        self._data = data
        self._data_pages = None
        self._current_page = 0

    def __len__(self):
        """Return the number of pages.
        1 page = 25 entries
        """
        if self.data is None:
            return 0

        # Ceil division
        return -(- (len(self.data["data"])) // 25)

    @property
    def data(self):
        return self._data

    def data_pages(self):
        """Return sorted data in multiple pages.
        Each page contains 25 entries.
        """
        if self._data_pages is None:
            data = [
                self.data["data"][i * 25: i * 25 + 25]
                for i in range(0, len(self))
            ]

            for page_index, page in enumerate(data):
                for entry_index, entry in enumerate(page):
                    data[page_index][entry_index] = wrap_data(entry)

            self._data_pages = data

        return self._data_pages

    def load(self):
        """This method should only be called once.
        """
        self.data_pages()
        self.page_select()
        self.entry_select()

    def reload(self):
        self.clear_items()
        self.load()

    def entry_embed(self, entry_index: int):
        return wrap_overview(
            self._data_pages[self._current_page][int(entry_index)]
        ).set_author(
            name=self.context.author.display_name[:256],
            icon_url=self.context.author.avatar.url
        )

    async def _page_callback(self, interaction: Interaction):
        await interaction.response.defer()
        self._current_page = int(interaction.data["values"][0])
        self.reload()
        await interaction.followup.edit_message(
            message_id=interaction.message.id,
            embed=self.entry_embed(0),
            view=self,
        )

    def page_select(self):
        if self.data is None:
            return

        if len(self) <= 1:
            return

        item = Select(
            placeholder=f"⭐ On page {self._current_page + 1} out of {(len(self)) if len(self) <= 25 else 25}",
            options=[
                SelectOption(
                    label=f"Page {i + 1}",
                    value=i
                )
                for index, i in enumerate(range(len(self)))
                if index < 25 # Max 25 options
            ]
        )

        item.callback = self._page_callback
        self.add_item(item)

    async def _entry_callback(self, interaction: Interaction):
        await interaction.response.defer()
        await interaction.followup.edit_message(
            message_id=interaction.message.id,
            embed=self.entry_embed(interaction.data["values"][0])
        )

    def entry_select(self):
        if self.data is None:
            return

        item = Select(
            placeholder="⭐ Choose an entry",
            options=[
                SelectOption(
                    label=entry["title"][:100],
                    value=index
                )
                for index, entry in enumerate(
                    self._data_pages[self._current_page]
                )
            ]
        )

        item.callback = self._entry_callback
        self.add_item(item)


class Seasonal(Entries_Modal):
    def __init__(
        self,
        context: Context,
    ):
        super().__init__(context, None)

    async def _get(self, **kwargs):
        self._data = await load_until_end(
            self.jikan.seasons,
            **kwargs
        )

    async def init_now(self):
        await self._get(
            extension="now"
        )

    async def init_upcoming(
        self,
        filter: Literal[
            "tv", "movie", "ova", "special", "ona", "music"
        ]=MISSING,
    ):
        await self._get(
            extension="upcoming",
            parameters={
                "filter": filter
            }
        )

    async def init_season(
        self,
        year: int,
        season: str,
    ):
        await self._get(
            year=year, season=season
        )


class Top(Entries_Modal):
    def __init__(
        self,
        context: Context,
    ):
        super().__init__(context, None)

    async def _get(self, **kwargs):
        self._data = await load_until_end(
            self.jikan.top,
            **kwargs
        )

    async def init_anime(
        self,
        entry_type: Literal[
            "tv", "movie", "ova", "special", "ona", "music"
        ]=MISSING,
        filter: Literal[
            "airing", "upcoming", "bypopularity", "favorite"
        ]=MISSING,
    ):
        await self._get(
            type="anime",
            parameters={
                "filter": filter,
                "limit": 625,
                "type": entry_type,
            },
        )

    async def init_manga(
        self,
        entry_type: Literal[
            "manga", "novel", "lightnovel", "oneshot", "doujin", "manhwa", "manhua"
        ]=MISSING,
        filter: Literal[
            "publishing", "upcoming", "bypopularity", "favorite"
        ]=MISSING,
    ):
        await self._get(
            type="anime",
            parameters={
                "filter": filter,
                "limit": 625,
                "type": entry_type,
            },
        )

    async def init_characters(self):
        await self._get(
            type="characters",
            parameters={
                "limit": 625
            },
        )

    async def init_people(self):
        await self._get(
            type="people",
            parameters={
                "limit": 625
            },
        )

    async def init_reviews(self):
        """Deprecated.
        """
        await self._get(
            type="reviews"
        )


class Random(Entries_Modal):
    def __init__(
        self,
        context: Context,
        number: int=3,
    ):
        super().__init__(context, None)

        if number > 25:
            self.number = 25
        elif number < 1:
            self.number = 1
        else:
            self.number = number

    async def _get(self, **kwargs):
        self._data = {"data": []}

        for _ in range(self.number):
            self._data["data"].append((
                await keep_retry(
                    self.jikan.random, **kwargs
                ))["data"]
            )

    async def init_anime(self):
        await self._get(type="anime")

    async def init_manga(self):
        await self._get(type="manga")

    async def init_characters(self):
        await self._get(type="characters")

    async def init_people(self):
        await self._get(type="people")

    async def init_users(self):
        await self._get(type="users")
