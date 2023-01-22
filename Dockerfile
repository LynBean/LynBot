FROM python:3.10-alpine
# For https://github.com/users/LynBean/packages/container/package/Yor
LABEL org.opencontainers.image.source https://github.com/LynBean/Yor

# Install bash and nano
RUN apk update
RUN apk add --no-cache bash
RUN apk add --no-cache nano

# Build the binary
ADD . /kim
WORKDIR /kim
RUN pip install --upgrade pip
RUN pip install -r ./requirements.txt

# Don't generate .pyc files, enable tracebacks on segfaults and disable STDOUT / STDERR buffering
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONFAULTHANDLER 1
ENV PYTHONHASHSEED 0
ENV PYTHONUNBUFFERED 1

CMD [ "python", "main.py" ]