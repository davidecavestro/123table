FROM groovy:4.0-jdk21

USER 0:0
RUN \
    --mount=type=cache,target=/var/cache/apt,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,sharing=locked \
    apt update && \
    export DEBIAN_FRONTEND=noninteractive && \
    apt-get --no-install-recommends install -y git

USER 1000:1000