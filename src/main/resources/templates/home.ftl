<!DOCTYPE html>
<html lang="zh-Hans">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width">
    <#-- <meta http-equiv="refresh" content="10"> -->
    <title>视频号管理</title>
    <link rel="stylesheet" href="../static/common.css">
    <#--<script src="../static/poll.js"></script>-->
    <style>
        :root {
            --margin-right: 25px;
        }

        .container {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            font-family: sans-serif;
        }

        .container > div,
        .container > button,
        .container > a,
        #closeStreaming button {
            margin-bottom: 10px;
        }

        .container > div:first-of-type {
            margin-top: 10px;
        }

        form {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }

        .input-group {
            display: flex;
            justify-content: left;
            align-items: center;
        }

        .input-group label {
            margin-right: 10px;
        }

        .trigger {
            display: flex;
            flex-direction: column;
        }

        .trigger div, .trigger button {
            margin-right: var(--margin-right);
        }

        .trigger-left, .trigger-right {
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .trigger-right {
            margin-top: 10px;
        }

        .trigger-right > form {
            flex-direction: row;
        }

        .add-trigger {
            flex-direction: row;
            margin-bottom: 10px;
        }

        .add-trigger > div {
            margin-right: var(--margin-right);
        }

        .trigger, .add-trigger {
            border: 1px solid rgba(178, 178, 178, .5);
            border-radius: 5px;
            padding: 15px 30px;
        }

        @media screen and (max-width: 768px) {
            * {
                font-size: 20px;
            }

            .nextCloseTime span {
                display: block;
            }

            .trigger-left,
            .trigger-right,
            .add-trigger {
                flex-direction: column;
            }

            .trigger-left > :nth-child(2) > span {
                display: block;
            }

            .trigger-left > div:first-child,
            .trigger-right,
            .add-trigger > div:first-child {
                margin-top: 0;
            }

            .trigger-right > form {
                flex-direction: column;
            }

            .trigger div,
            .trigger button,
            .add-trigger div,
            .add-trigger button {
                margin-right: 0;
            }

            .trigger-left > *,
            .trigger-right > form > *,
            .add-trigger > * {
                margin-top: 15px;
            }
        }
    </style>
</head>
<body>
<#--todo: adapt mobile page-->
<div class="container">
    <#if exception??>
        <div>程序发生异常:${exception}</div>
    </#if>

    <div>是否已登录: ${isLoggedIn?string('是', '否')}</div>
    <#if !isLoggedIn>
        <a href="/login">去登录</a>
    <#else >
        <a href="/logout">退出登录</a>
    <#-- <div>是否已开播: ${isStreaming?string('是', '否')}</div> -->
        <div id="isStreaming">是否已开播: 正在获取...</div>
    <#-- TODO: add animation when submitting -->
    <#-- TODO: close confirmation, e.g. alert -->
        <form id="closeStreaming" style="display: none" action="/closeStreaming" method="post">
            <button type="button" onclick="handleCloseStreaming()">关闭直播</button>
            <button id="closeStreamingButton" type="submit" style="display: none"></button>
        </form>

        <a href="/income">查看收入</a>

        <#if nextTriggerTime??>
            <div class="nextCloseTime">下次关闭直播时间: <span>${nextTriggerTime}</span></div>
        </#if>

        <#list triggers as trigger>
            <div class="trigger">
                <div class="trigger-left">
                    <div>触发器名称: <span>${trigger.name[trigger.name?last_index_of("-") + 1..]}</span></div>
                    <div>
                        <#--下次触发时间: <span>${trigger.nextTriggerTime.format("yyyy年MM月dd日HH时mm分")}</span>-->
                        下次触发时间: <span>${trigger.nextTriggerTime.format("MM月dd日HH时mm分")}</span>
                    </div>
                    <div>是否重复触发: <span>${trigger.repeated?string('是', '否')}</span></div>
                </div>
                <div class="trigger-right">
                    <form action="/trigger/addOrModifyTrigger" method="post">
                        <div class="input-group">
                            <label for="time">修改为: </label>
                            <input id="time" name="time" type="time"
                                   value="${trigger.nextTriggerTime.format("HH:mm")}">
                        </div>
                        <div class="input-group">
                            <#-- todo: set by current state -->
                            <label for="repeated">是否重复触发: </label>
                            <input id="repeated" name="repeated" type="checkbox" value="true">
                        </div>
                        <input type="hidden" name="triggerName" value="${trigger.name}">
                        <input type="hidden" name="changeTriggerTime" value="true">
                        <button type="submit">修改</button>
                    </form>
                    <form action="/trigger/removeTrigger" method="post">
                        <input type="hidden" name="triggerName" value="${trigger.name}">
                        <button type="submit">删除</button>
                    </form>
                </div>
            </div>
        </#list>

        <form class="add-trigger" action="/trigger/addOrModifyTrigger" method="post">
            <#-- todo: show current time -->
            <div class="input-group">
                <label for="timeForAdd">触发时间: </label>
                <input id="timeForAdd" name="time" type="time" value="12:00">
            </div>
            <div class="input-group">
                <label for="repeatedForAdd">是否重复触发: </label>
                <input id="repeatedForAdd" name="repeated" type="checkbox" value="true">
            </div>
            <input type="hidden" name="changeTriggerTime" value="false">
            <button type="submit">添加</button>
        </form>
    </#if>

    <a href="/">刷新状态</a>
</div>
<script>
    const handleCloseStreaming = (event) => {
        const confirmCloseStreaming = confirm('确定关闭直播吗？');
        if (confirmCloseStreaming) {
            const closeStreamingButton = document.querySelector('#closeStreamingButton');
            closeStreamingButton.click();
        }
    };

    onload = (event) => {
        let isStreamingElement = document.querySelector('#isStreaming');
        let closeStreamingElement = document.querySelector('#closeStreaming');

        fetch("/isStreaming")
            .then((response) => response.json())
            .then((result) => {
                let isStreaming = result.isStreaming;

                isStreamingElement.textContent = '是否已开播: ' + (isStreaming ? '是' : '否')
                if (isStreaming) {
                    closeStreamingElement.style.display = 'initial'
                }
            })
            .catch((error) => {
                isStreamingElement.textContent = '是否已开播: 获取开播状态失败'
            })
    }
</script>
</body>
</html>
