# run
```shell
docker run --detach --name wechat --hostname ubuntu --restart=unless-stopped -p 9000:9000 wechat
```

# build
```shell
docker build -t dokcn/wechat-channels --progress plain --build-arg PROXY='-x host.docker.internal:10809' .
```
```shell
docker build -t dokcn/wechat-channels --build-arg PROXY='-x host.docker.internal:10809' .
```
