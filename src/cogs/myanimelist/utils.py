
from typing import Coroutine, Dict, Any, List, Optional, Union, Callable
from discord import Embed
from jikanpy.exceptions import APIException
import asyncio

from .constants import *


def wrap_data(data: Dict[str, Any]) -> Dict[str, Any]:
    """Wraps the data from the given response.
    """
    data = data.get("data", data)
    import json
    json.dump(data, open(".json", "w"), indent=4)

    # Common

    data["id"] = data["mal_id"]
    data["url"] = data["url"]
    data["image_url"] = data["images"]["jpg"]["image_url"]

    # Anime

    data["trailer_url"] = data["trailer"]["url"] \
        if data.get("trailer") else None

    data["trailer_thumbnail"] = data["trailer"]["images"]["image_url"] \
        if data.get("trailer") else None

    data["title"] = data.get("title")
    data["title_jp"] = data.get("title_japanese")
    data["title_en"] = data.get("title_english")
    data["type"] = data.get("type")
    data["source"] = data.get("source")
    data["episodes"] = data.get("episodes")
    data["status"] = data.get("status")

    data["aired_string"] = data["aired"]["string"] \
        if data.get("aired") else None

    data["duration"] = data.get("duration")
    data["rating"] = data.get("rating")
    data["score"] = data.get("score")
    data["scored_by"] = data.get("scored_by")
    data["rank"] = data.get("rank")
    data["popularity"] = data.get("popularity")
    data["members"] = data.get("members")
    data["favorites"] = data.get("favorites")
    data["synopsis"] = data.get("synopsis")
    data["background"] = data.get("background")
    data["season"] = data.get("season")
    data["year"] = data.get("year")


    data["broadcast"] = data["broadcast"]["string"] \
        if data.get("broadcast") else None

    data["producers"] = ", ".join([k["name"] for k in data["producers"]]) \
        if data.get("producers") else None

    data["licensors"] = ", ".join([k["name"] for k in data["licensors"]]) \
        if data.get("licensors") else None

    data["studios"] = ", ".join([k["name"] for k in data["studios"]]) \
        if data.get("studios") else None

    data["genres"] = ", ".join([k["name"] for k in data["genres"]]) \
        if data.get("genres") else None

    data["explicit_genres"] = ", ".join([k["name"] for k in data["explicit_genres"]]) \
        if data.get("explicit_genres") else None

    data["themes"] = ", ".join([k["name"] for k in data["themes"]]) \
        if data.get("themes") else None

    data["demographics"] = ", ".join([k["name"] for k in data["demographics"]]) \
        if data.get("demographics") else None

    data["external"]: List[Dict[str, str]] = data.get("external")

    # Manga

    data["chapters"] = data.get("chapters")
    data["volumes"] = data.get("volumes")

    data["published_string"] = data["published"]["prop"]["string"] \
        if data.get("published") else None

    data["authors"] = ", ".join([k["name"] for k in data["authors"]]) \
        if data.get("authors") else None

    data["serializations"] = ", ".join([k["name"] for k in data["serializations"]]) \
        if data.get("serializations") else None

    # Characters

    data["title"] = data.get("name", data["title"])
    data["title_jp"] = data.get("name_kanji", data["title_jp"])

    data["nicknames"] = ", ".join(data.get("nicknames")) \
        if data.get("nicknames") else None

    data["about"] = data.get("about", data["synopsis"])

    # People

    data["web_url"] = data.get("website_url")
    data["given_name"] = data.get("given_name")
    data["family_name"] = data.get("family_name")

    data["alternate_names"] = ", ".join(data.get("alternate_names")) \
        if data.get("alternate_names") else None

    data["birthday"] = data.get("birthday")

    # Users

    data["title"] = data.get("username", data["title"])
    data["last_online"] = data.get("last_online")
    data["gender"] = data.get("gender")
    data["location"] = data.get("location")
    data["joined"] = data.get("joined")

    anime = data.get("statistics", {}).get("anime")
    manga = data.get("statistics", {}).get("manga")

    data["anime_days_watched"] = anime.get("days_watched") \
        if anime else None

    data["anime_mean_score"] = anime.get("mean_score") \
        if anime else None

    data["anime_watching"] = anime.get("watching") \
        if anime else None

    data["anime_completed"] = anime.get("completed") \
        if anime else None

    data["anime_on_hold"] = anime.get("on_hold") \
        if anime else None

    data["anime_dropped"] = anime.get("dropped") \
        if anime else None

    data["anime_plan_to_watch"] = anime.get("plan_to_watch") \
        if anime else None

    data["anime_total_entries"] = anime.get("total_entries") \
        if anime else None

    data["anime_rewatched"] = anime.get("rewatched") \
        if anime else None

    data["anime_episodes_watched"] = anime.get("episodes_watched") \
        if anime else None

    data["manga_days_read"] = manga.get("days_read") \
        if manga else None

    data["manga_mean_score"] = manga.get("mean_score") \
        if manga else None

    data["manga_reading"] = manga.get("reading") \
        if manga else None

    data["manga_completed"] = manga.get("completed") \
        if manga else None

    data["manga_on_hold"] = manga.get("on_hold") \
        if manga else None

    data["manga_dropped"] = manga.get("dropped") \
        if manga else None

    data["manga_plan_to_read"] = manga.get("plan_to_read") \
        if manga else None

    data["manga_total_entries"] = manga.get("total_entries") \
        if manga else None

    data["manga_reread"] = manga.get("reread") \
        if manga else None

    data["manga_chapters_read"] = manga.get("chapters_read") \
        if manga else None

    data["manga_volumes_read"] = manga.get("volumes_read") \
        if manga else None

    if isinstance(data["favorites"], dict):
        data["user_favorites"]: Dict = data["favorites"]
        data["favorites"] = None

    data["user_updates"]: Dict = data.get("updates")

    # Reviews

    data["author"] = data.get("user", {}).get("username")
    data["title"] = data.get("anime", {}).get("title", data["title"]) \
        if isinstance(data.get("anime"), dict) else data["title"]

    data["image_url"] = data["anime"]["images"]["jpg"]["image_url"] \
        if isinstance(data.get("anime"), dict) else data["image_url"]

    data["review_type"] = data.get("type")
    data["date"] = data.get("date")
    data["review"] = data.get("review")
    data["is_spoiler"] = data.get("is_spoiler")
    data["is_preliminary"] = data.get("is_preliminary")
    data["episodes_watched"] = data.get("episodes_watched")

    return data

def wrap_overview(data) -> Embed:
    embed = Embed()

    # Anime/Manga
    if data["score"]:
        embed.add_field(
            name="Score", value=f"⭐ {data['score']} / 10",
        )
    if data["rank"]:
        embed.add_field(
            name="Rank", value=f"🏆 #{data['rank']}",
        )
    if data["popularity"]:
        embed.add_field(
            name="Popularity", value=f"🔥 #{data['popularity']}"
        )
    if data["members"]:
        embed.add_field(
            name="Members", value=f"😍 {data['members']:,}"
        )
    if data["type"]:
        embed.add_field(
            name="Type", value=f"📺 {data['type']}"
        )
    if data["source"]:
        embed.add_field(
            name="Source", value=f"📖 {data['source']}"
        )
    if data["episodes"]:
        embed.add_field(
            name="Episodes", value=f"🆕 {data['episodes']}"
        )
    if data["volumes"]:
        embed.add_field(
            name="Volumes", value=f"🆕 {data['volumes']}"
        )
    if data["chapters"]:
        embed.add_field(
            name="Chapters", value=f"🆕 {data['chapters']}"
        )
    if data["aired_string"]:
        embed.add_field(
            name="Aired", value=f"📅 {data['aired_string']}"
        )
    if data["published_string"]:
        embed.add_field(
            name="Published", value=f"📅 {data['published_string']}"
        )

    # Characters/People

    if data["nicknames"]:
        embed.add_field(
            name="Nicknames", value=data["nicknames"]
        )
    if data["given_name"]:
        embed.add_field(
            name="Given_Name", value=data["given_name"]
        )
    if data["family_name"]:
        embed.add_field(
            name="Family_Name", value=data["family_name"]
        )
    if data["alternate_names"]:
        embed.add_field(
            name="Alternate_Names", value=data["alternate_names"]
        )
    if data["birthday"]:
        embed.add_field(
            name="Birthday", value=data["birthday"]
        )

    # Users

    if data["last_online"]:
        embed.add_field(
            name="Last_Online", value=data["last_online"]
        )
    if data["gender"]:
        embed.add_field(
            name="Gender", value=data["gender"]
        )
    if data["location"]:
        embed.add_field(
            name="Location", value=data["location"]
        )
    if data["joined"]:
        embed.add_field(
            name="Joined_At", value=data["joined"]
        )
    if data["background"]:
        embed.set_footer(
            text=data["background"][:2048],
            icon_url=MAL_ICO_URL
        )
    if data["image_url"]:
        embed.set_thumbnail(url=data["image_url"])

    embed.title = data["title"][:256] if data["title"] else data["title"][:256]
    embed.description = data["about"][:4096] if data["about"] else "Not available."
    embed.url = data["url"]
    return embed

def wrap_fullview(data) -> Embed:
    embed = wrap_overview(data)

    if data["rating"]:
        embed.add_field(
            name="Rating", value=f"🔞 {data['rating']}"
        )
    if data["broadcast"]:
        embed.add_field(
            name="Broadcast", value=f"📺 {data['broadcast']}"
        )
    if data["id"]:
        embed.add_field(
            name="ID", value=f"🆔 {data['id']}"
        )
    if data["scored_by"]:
        embed.add_field(
            name="Scored_By", value=f"{data['scored_by']:,}"
        )
    if data["favorites"]:
        embed.add_field(
            name="Favorites", value=f"{data['favorites']:,}"
        )
    if data["season"]:
        embed.add_field(
            name="Season", value=f"{data['season']}"
        )
    if data["year"]:
        embed.add_field(
            name="Year", value=f"{data['year']}"
        )
    if data["duration"]:
        embed.add_field(
            name="Duration", value=f"{data['duration']}"
        )
    if data["producers"]:
        embed.add_field(
            name="Producers", value=f"{data['producers']}"
        )
    if data["authors"]:
        embed.add_field(
            name="Authors", value=f"{data['authors']}"
        )
    if data["licensors"]:
        embed.add_field(
            name="Licensors", value=f"{data['licensors']}"
        )
    if data["studios"]:
        embed.add_field(
            name="Studios", value=f"{data['studios']}"
        )
    if data["serializations"]:
        embed.add_field(
            name="Serializations", value=f"{data['serializations']}"
        )
    if data["genres"]:
        embed.add_field(
            name="Genres", value=f"{data['genres']}"
        )
    if data["themes"]:
        embed.add_field(
            name="Themes", value=f"{data['themes']}"
        )
    if data["demographics"]:
        embed.add_field(
            name="Demographics", value=f"{data['demographics']}"
        )
    if data["background"]:
        embed.set_footer(
            text=data["background"][:2048],
            icon_url=MAL_ICO_URL
        )
    if data["image_url"]:
        embed.set_image(url=data["image_url"])

    embed.title = data["title_jp"][:256] if data["title_jp"] else data["title"][:256]
    embed.description = data["about"][:4096] if data["about"] else "Not available."
    embed.url = data["url"]

    return embed


async def keep_retry(
    func: Callable,
    **kwargs
):
    """Keep retrying for Jikan response until it succeeds.
    """
    while True:
        try:
            return await func(**kwargs)
        except APIException:
            await asyncio.sleep(1)
            continue

async def load_until_end(
    func: Callable,
    **kwargs
):
    """Load all pages of the data.
    """
    page = 1
    data = {"data": []}

    while True:
        response = await keep_retry(func, page=page, **kwargs)
        data["data"] += response["data"]

        # If the number of pages is greater than 25, then we will stop loading
        # As one select menu can only have 25 options
        if -(- (len(data["data"])) // 25) > 25:
            pass
        elif response["pagination"]["has_next_page"]:
            page += 1
            continue

        return data
