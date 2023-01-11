
from aiohttp import ClientSession
from collections import deque
from dataclasses import dataclass
from datetime import datetime
from typing import Deque

from src.constants import *
from src.utils import *


@dataclass
class Game:
    title: str
    description: str
    image_url: str
    pricing: str
    seller: str
    fmt_promo_date: str
    ongoing: bool

@dataclass
class GamePool:
    def __post_init__(self):
        self.pool: Deque[Game] = deque()

    def add(self, game: Game):
        self.pool.append(game)

    def __len__(self):
        return len(self.pool)

    def __iter__(self):
        return iter(self.pool)

    def to_dict(self):
        return {"data": [game.__dict__ for game in self.pool]}


class EpicGames:
    URL = "https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions"

    def __init__(self):
        self.games: GamePool = GamePool()

    async def start(self) -> None:
        self._data = await self._get()
        self._init_games(self._data)

    async def _get(self) -> dict:
        async with ClientSession() as session:
            async with session.get(
                self.URL,
                headers={
                    "User-Agent": USER_AGENT
                }
            ) as response:

                return await response.json()

    def _init_games(self, data: dict) -> None:
        game_with_promo = [
            game for game in data["data"]["Catalog"]["searchStore"]["elements"]
            if game["promotions"]
        ]

        for game in game_with_promo:
            promo_now = game["promotions"]["promotionalOffers"]
            promo_upcoming = game["promotions"]["upcomingPromotionalOffers"]

            if promo_now:
                ongoing = True
                promo = promo_now[0]["promotionalOffers"][0]
                end_date = datetime.fromisoformat(promo["endDate"].replace("Z", "+00:00"))
                fmt_promo_date = f"Free Now - {end_date.strftime('%b %d at %I:%M %p')}"

            elif promo_upcoming:
                ongoing = False
                promo = promo_upcoming[0]["promotionalOffers"][0]
                start_date = datetime.fromisoformat(promo["startDate"].replace("Z", "+00:00"))
                end_date = datetime.fromisoformat(promo["endDate"].replace("Z", "+00:00"))
                fmt_promo_date = f"Free {start_date.strftime('%b %d')} - {end_date.strftime('%b %d')}"

            if image := game["keyImages"]:
                image_url = image[-1]["url"]

            self.games.add(
                Game(
                    title=game["title"],
                    description=game["description"],
                    image_url=image_url,
                    pricing=game["price"]["totalPrice"]["fmtPrice"]["originalPrice"],
                    seller=game["seller"]["name"],
                    fmt_promo_date=fmt_promo_date,
                    ongoing=ongoing,
                )
            )
