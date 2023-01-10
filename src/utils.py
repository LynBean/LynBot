
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
