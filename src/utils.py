
from discord import Embed
from os import PathLike
from os.path import exists, join
from pathlib import Path
from typing import Any
import os
import platform

from .constants import *

class _MissingSentinel:
    __slots__ = ()

    def __eq__(self, other) -> bool:
        return False

    def __bool__(self) -> bool:
        return False

    def __hash__(self) -> int:
        return 0

    def __repr__(self):
        return '...'

MISSING: Any = _MissingSentinel()

def version() -> str:
    """Returns the current version of the bot.
    """
    from . import __version__
    return __version__

def app_path() -> PathLike[str]:
    """Returns the path to the application data directory.
    """
    uname = platform.system()
    if uname == 'Windows':
        return join(os.environ['APPDATA'], 'Kim', 'Yor')
    elif uname == 'Linux':
        return join(os.environ['HOME'], '.kim', 'yor')
    elif uname == 'Darwin':
        return join(os.environ['HOME'], 'Library', 'Application Support', 'Kim', 'Yor')
    else:
        return join(os.getcwd(), '.kim', 'yor')

def make_dir(dir_name: str=MISSING) -> PathLike[str]:
    """Generates a directory if it does not exist.
    If path is not specified, the directory will be generated in the application data directory.
    """
    path = app_path() if dir_name is MISSING \
        else join(app_path(), dir_name)

    os.makedirs(path, exist_ok=True)
    return path

def touch_file(filename: str, dir_name: str, exist_ok: bool=True) -> PathLike[str]:
    """Generates a file if it does not exist.
    If directory is not specified, the file will be generated in the application data directory.
    """
    path = app_path() if dir_name is MISSING \
        else join(app_path(), dir_name)

    make_dir(path)
    path = join(path, filename)
    Path(path).touch(exist_ok=exist_ok)
    return path

def embed_maker(title: str=MISSING, description: str=MISSING, url: str=MISSING, **fields: str) -> Embed:
    """Returns an embed with the specified fields.
    """
    embed = Embed()
    if title is not MISSING:
        embed.title = title
    if description is not MISSING:
        embed.description = description
    if url is not MISSING:
        embed.url = url
    for name, value in fields.items():
        embed.add_field(name=name, value=value)

    return embed

def defer_embed() -> Embed:
    """Returns an embed that indicates that the bot is processing the command.
    """
    return Embed(title="Chotto matte kudasai")