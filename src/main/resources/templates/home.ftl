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
            justify-content: center;
            align-items: center;
        }
    </style>
</head>
<body>
<div class="container">
    <#if exception??>
        <div>程序发生异常：${exception}</div>
    </#if>

    <div>是否已登录：${isLoggedIn?string('是', '否')}</div>
    <#if !isLoggedIn>
        <a href="/login">去登录</a>
    <#else >
        <a href="/logout">退出登录</a>
        <div>是否已开播：${isStreaming?string('是', '否')}</div>
        <#if isStreaming>
            <form action="/closeStreaming" method="post">
                <button type="submit">关闭直播</button>
            </form>
        </#if>
    </#if>

    <a href="/">刷新</a>

    <#if nextTriggerTime??>
        <#--<div>下次关闭直播时间：${nextTriggerTime?string["yyyy年MM月dd日 HH时mm分"]}</div>-->
        <div>下次关闭直播时间：${nextTriggerTime}</div>
        <form action="/changeCloseStreamingTime" method="post">
            <div class="input-group">
                <label for="timeToChange">修改为：</label>
                <input id="timeToChange" name="timeToChange" type="time" value="${timePlaceholder}">
            </div>
            <div class="input-group">
                <label for="isPermanent">是否永久修改：</label>
                <input id="isPermanent" name="isPermanent" type="checkbox" value="true">
            </div>
            <button type="submit">修改</button>
        </form>
    </#if>
</div>
</body>
</html>
