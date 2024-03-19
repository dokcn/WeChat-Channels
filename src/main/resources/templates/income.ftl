<!DOCTYPE html>
<html lang="zh-Hans">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width">
    <title>直播收入</title>
    <link rel="stylesheet" href="../static/common.css">
    <style>
        .row {
            display: flex;
            flex-direction: column;
            padding: 10px 10px 10px 0;
        }

        .row:nth-of-type(n+2) {
            border-top: 1px solid gray;
        }

        .row-group {
            display: flex;
        }

        .row-group:nth-child(n+2) {
            margin-top: 10px;
        }

        .row div {
            font-size: 0.9em;
            margin-right: 30px;
        }

        .row div:last-of-type {
            margin-right: 0;
        }

        a[href="/"] {
            margin-top: 10px;
        }
    </style>
</head>
<body>
<#-- todo: using grid layout -->
<div class="container">
    <h2>近期直播收入</h2>
    <#list incomeInfoList as incomeInfo>
        <div class="row">
            <div class="row-group">
                <div>直播标题: ${incomeInfo.streamingTitle()}</div>
                <div>直播开始时间: ${incomeInfo.streamingTime()}</div>
            </div>
            <div class="row-group">
                <div>直播持续时间: ${incomeInfo.streamingDuration()}</div>
                <div>观看人数: ${incomeInfo.numberOfWatch()}</div>
                <div style="font-weight: bolder">收入: ${incomeInfo.income()}</div>
            </div>
        </div>
    </#list>
    <a href="/">返回首页</a>
</div>
</body>
</html>
