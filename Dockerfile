FROM python:3.10-slim AS build

# Update first
RUN apt-get update
RUN apt-get upgrade -y
RUN apt-get install git nano vim -y

ADD . /kim
WORKDIR /kim
RUN pip install --upgrade pip
RUN pip install -r ./requirements.txt

# Don't generate .pyc files, enable tracebacks on segfaults and disable STDOUT / STDERR buffering
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONFAULTHANDLER 1
ENV PYTHONUNBUFFERED 1
ENV PYTHONHASHSEED 0

# For https://github.com/users/LynBean/packages/container/package/Yor
LABEL org.opencontainers.image.source https://github.com/LynBean/Yor

CMD ["python", "main.py"]