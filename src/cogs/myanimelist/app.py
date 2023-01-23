
from aiohttp import ClientSession
from jikanpy import AioJikan
from typing import TypeVar, Union, Optional, Dict, Any, Literal, List, Tuple
from copy import deepcopy

from discord import Embed, Interaction, SelectOption, ButtonStyle
from discord.ext.commands import Context
from discord.ui import select, Select, View, button, Button
import discord

from src.config import Config
from src.constants import *
from src.utils import *


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

    def _wrap_data(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Wraps the data from the given response.
        """
        data = data["data"]
        result = deepcopy(data)
        result["id"] = result["mal_id"]
        result["url"] = result["url"]
        result["image_url"] = result["images"]["jpg"]["image_url"]

        result["trailer_url"] = result["trailer"]["url"] \
            if result.get("trailer") else None
        result["trailer_thumbnail"] = result["trailer"]["images"]["image_url"] \
            if result.get("trailer") else None

        result["title"] = result["title"]
        result["title_jp"] = result["title_japanese"]
        result["title_en"] = result["title_english"]
        result["type"] = result["type"]

        result["source"] = result.get("source")

        result["episodes"] = result.get("episodes")
        result["chapters"] = result.get("chapters")
        result["volumes"] = result.get("volumes")

        result["status"] = result["status"]

        result["airing"] = result.get("airing")
        result["publishing"] = result.get("publishing")

        result["aired_string"] = result["aired"]["string"] \
            if result.get("aired") else None
        result["published_string"] = result["published"]["string"] \
            if result.get("published") else None

        result["rating"] = result.get("rating")

        result["score"] = result["score"]
        result["scored_by"] = result["scored_by"]
        result["rank"] = result["rank"]
        result["popularity"] = result["popularity"]
        result["members"] = result["members"]
        result["favorites"] = result["favorites"]
        result["synopsis"] = result["synopsis"]
        result["background"] = result["background"]

        result["season"] = result.get("season")
        result["year"] = result.get("year")
        result["broadcast"] = result["broadcast"]["string"] \
            if result.get("broadcast") else None
        result["duration"] = result.get("duration")

        result["producers"] = ", ".join([p["name"] for p in result["producers"]]) \
            if result.get("producers") else None
        result["authors"] = ", ".join([a["name"] for a in result["authors"]]) \
            if result.get("authors") else None
        result["licensors"] = ", ".join([l["name"] for l in result["licensors"]]) \
            if result.get("licensors") else None
        result["studios"] = ", ".join([s["name"] for s in result["studios"]]) \
            if result.get("studios") else None
        result["serializations"] = ", ".join([s["name"] for s in result["serializations"]]) \
            if result.get("serializations") else None
        result["genres"] = ", ".join([g["name"] for g in result["genres"]]) \
            if result.get("genres") else None
        result["themes"] = ", ".join([t["name"] for t in result["themes"]]) \
            if result.get("themes") else None
        result["demographics"] = ", ".join([d["name"] for d in result["demographics"]]) \
            if result.get("demographics") else None

        return result

    async def anime(
        self,
        id: int,
        extension: Optional[str]=None,
        page: Optional[int]=None,
    ) -> Dict[str, Any]:
        """Returns the anime information for the given id.
        """
        result = await super().anime(id, extension, page)
        if extension is not None:
            return result

        return self._wrap_data(result)

    async def manga(
        self,
        id: int,
        extension: Optional[str]=None,
        page: Optional[int]=None,
    ) -> Dict[str, Any]:
        """Returns the manga information for the given id.
        """
        result = await super().manga(id, extension, page)
        if extension is not None:
            return result

        return self._wrap_data(result)

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
        entry_type: Literal["anime", "manga"]
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
            (entry["name"], str(entry["id"]))
            for index, entry in enumerate(response["categories"][0]["items"])
            if index <= 25
        ]


class Modal(discord.ui.View):
    def __init__(
        self,
        context: Context,
        id: int,
        timeout: Optional[float]=MISSING
    ):
        super().__init__(timeout=timeout)
        self.jikan = Jikan()
        self.id = id
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

        fields: Dict[str, str] = {}
        if self._data["score"]:
            fields["Score"] = f"⭐ {self._data['score']} / 10"
        if self._data["rank"]:
            fields["Ranking"] = f"🏆 #{self._data['rank']}"
        if self._data["popularity"]:
            fields["Popularity"] = f"🔥 #{self._data['popularity']}"
        if self._data["members"]:
            fields["Members"] = f"😍 {self._data['members']:,}"
        if self._data["type"]:
            fields["Type"] = f"📺 {self._data['type']}"
        if self._data["source"]:
            fields["Source"] = f"📖 {self._data['source']}"
        if self._data["episodes"]:
            fields["Episodes"] = f"🆕 {self._data['episodes']}"
        if self._data["volumes"]:
            fields["Volumes"] = f"🆕 {self._data['volumes']}"
        if self._data["chapters"]:
            fields["Chapters"] = f"🆕 {self._data['chapters']}"
        if self._data["aired_string"]:
            fields["Aired"] = f"📅 {self._data['aired_string']}"
        if self._data["published_string"]:
            fields["Published"] = f"📅 {self._data['published_string']}"

        self._overview = embed_maker(
            title=self._data["title"][:256],
            description=self._data["synopsis"][:4096],
            url=self._data["url"],
            **fields,
        ).set_author(
            name=self.context.author.display_name[:256],
            icon_url=self.context.author.avatar.url,
        ).set_footer(
            text=self._data["background"][:2048] if self._data["background"] else None,
            icon_url="https://image.myanimelist.net/ui/OK6W_koKDTOqqqLDbIoPAiC8a86sHufn_jOI-JGtoCQ",
        ).set_thumbnail(
            url=self._data["image_url"],
        )

        return self._overview

    async def fullview(self) -> Embed:
        if self._fullview is not None:
            return self._fullview

        await self.data()

        fields: Dict[str, str] = {}
        if self._data["rating"]:
            fields["Rating"] = f"🔞 {self._data['rating']}"
        if self._data["broadcast"]:
            fields["Broadcast"] = f"📺 {self._data['broadcast']}"
        if self._data["id"]:
            fields["ID"] = self._data["id"]
        if self._data["scored_by"]:
            fields["Scored by"] = f"{self._data['scored_by']:,}"
        if self._data["favorites"]:
            fields["Favorites"] = f"{self._data['favorites']:,}"
        if self._data["season"]:
            fields["Season"] = self._data["season"]
        if self._data["year"]:
            fields["Year"] = self._data["year"]
        if self._data["duration"]:
            fields["Duration"] = self._data["duration"]
        if self._data["producers"]:
            fields["Producers"] = self._data["producers"]
        if self._data["authors"]:
            fields["Authors"] = self._data["authors"]
        if self._data["licensors"]:
            fields["Licensors"] = self._data["licensors"]
        if self._data["studios"]:
            fields["Studios"] = self._data["studios"]
        if self._data["serializations"]:
            fields["Serializations"] = self._data["serializations"]
        if self._data["genres"]:
            fields["Genres"] = self._data["genres"]
        if self._data["themes"]:
            fields["Themes"] = self._data["themes"]
        if self._data["demographics"]:
            fields["Demographics"] = self._data["demographics"]

        self._fullview = embed_maker(
            init_embed=deepcopy(
                await self.overview()
            ),
            title=self._data["title_jp"] or self._data["title"],
            **fields,
        ).set_image(
            url=self._data["image_url"],
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

        if self._links is None:
            await self.links(self.__class__.__name__.lower())
            await self.set_link_buttons()

        await interaction.followup.edit_message(
            message_id=interaction.message.id,
            embed=await getattr(self, interaction.data["values"][0])(),
            view=self,
        )

    async def links(self, endpoint: Literal["anime", "manga"]):
        """Initialize links.
        """
        if self._links is not None:
            return self._links

        external = await getattr(self.jikan, endpoint)(id=self.id, extension="external")
        streaming = {}

        if endpoint != "manga":
            streaming = await self.jikan.anime(id=self.id, extension="streaming")

        self._links = external["data"] + streaming.get("data", [])
        return self._links

    async def set_link_buttons(self):
        """Initialize link buttons.
        """
        for data in self._links:
            self.add_item(
                Button(
                    style=ButtonStyle.url,
                    label=data["name"],
                    url=data["url"],
                )
            )


class Anime(Modal):
    def __init__(
        self,
        context: Context,
        id: int,
    ):
        super().__init__(context, id)

    async def data(self) -> Dict[str, Any]:
        if self._data is None:
            self._data = await self.jikan.anime(int(self.id))

        await super().data()


class Manga(Modal):
    def __init__(
        self,
        context: Context,
        id: int,
    ):
        super().__init__(context, id)

    async def data(self) -> Dict[str, Any]:
        if self._data is None:
            self._data = await self.jikan.manga(int(self.id))

        await super().data()
