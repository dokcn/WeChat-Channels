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

        .container div {
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

        .trigger {
            display: flex;
            flex-direction: column;
            border: 1px solid gray;
            padding: 10px 10px 0 10px;
        }

        .trigger-left, .trigger-right {
            display: flex;
        }

        .trigger-left > div {
            margin-right: var(--margin-right);
        }

        .trigger-right div, .trigger-right button {
            margin-right: var(--margin-right);
        }

        .trigger-right > form:first-child {
            display: flex;
            flex-direction: row;
        }

        .add-trigger {
            display: flex;
            flex-direction: row;
            border: 1px solid gray;
            padding: 10px 10px 0 10px;
            margin-bottom: 10px;
        }

        .add-trigger > div {
            margin-right: var(--margin-right);
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
        <div>是否已开播: ${isStreaming?string('是', '否')}</div>
        <#if isStreaming>
            <form action="/closeStreaming" method="post">
                <button type="submit">关闭直播</button>
            </form>
        </#if>

        <a href="/income">查看收入</a>

        <#if nextTriggerTime??>
            <div>下次关闭直播时间: ${nextTriggerTime}</div>
        </#if>

        <#-- todo: margin error -->
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
                <label for="time">触发时间: </label>
                <input id="time" name="time" type="time" value="12:00">
            </div>
            <div class="input-group">
                <label for="repeated">是否重复触发: </label>
                <input id="repeated" name="repeated" type="checkbox" value="true">
            </div>
            <input type="hidden" name="changeTriggerTime" value="false">
            <button type="submit">添加</button>
        </form>
    </#if>

    <a href="/">刷新状态</a>
</div>
</body>
</html>
