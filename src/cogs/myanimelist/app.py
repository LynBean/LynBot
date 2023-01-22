
from aiohttp import ClientSession
from jikanpy import AioJikan
from typing import TypeVar, Union, Optional, Dict, Any, Literal, List, Tuple

from src.config import Config
from src.constants import *


MalT = TypeVar("MalT", bound="Mal")
config = Config()


class Jikan(AioJikan):
    def __init__(self):
        super().__init__()

    async def _request(self,
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

    async def anime(self,
                    id: int,
                    extension: Optional[str]=None,
                    page: Optional[int]=None,
                    ) -> Dict[str, Any]:
        """Returns the anime information for the given id.
        """
        result = await super().anime(id, extension, page)
        result = result["data"]

        return {
            "id": result["mal_id"],
            "url": result["url"],
            "image_url": result["images"]["jpg"]["image_url"],
            "trailer_url": result["trailer"]["url"],
            "trailer_thumbnail": result["trailer"]["images"]["image_url"],
            "title": result["title"],
            "title_jp": result["title_japanese"],
            "title_en": result["title_english"],
            "type": result["type"],
            "source": result["source"],
            "releases": result["episodes"],
            "status": result["status"],
            "airing": result["airing"],
            "aired_string": result["aired"]["string"],
            "rating": result["rating"],
            "score": result["score"],
            "scored_by": result["scored_by"],
            "rank": result["rank"],
            "popularity": result["popularity"],
            "members": result["members"],
            "favorites": result["favorites"],
            "synopsis": result["synopsis"],
            "background": result["background"],
            "season": result["season"],
            "year": result["year"],
            "broadcast": result["broadcast"]["string"],
            "producers": ", ".join([p["name"] for p in result["producers"]]),
            "licensors": ", ".join([l["name"] for l in result["licensors"]]),
            "studios": ", ".join([s["name"] for s in result["studios"]]),
            "genres": ", ".join([g["name"] for g in result["genres"]]),
            "themes": ", ".join([t["name"] for t in result["themes"]]),
            "duration": result["duration"],
        }


class Mal:
    API_URL = "https://api.myanimelist.net/v2/"
    WEB_URL = "https://myanimelist.net/"

    def __init__(self):
        self.session = None

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

    async def _request(self,
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

    async def search(self,
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










