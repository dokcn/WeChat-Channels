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
            border-top: 1px solid rgba(178, 178, 178, .5);
        }

        .row-group {
            display: flex;
            justify-content: space-between;
        }

        .row-group:nth-child(n+2) {
            margin-top: 10px;
        }

        .row div {
            margin-right: 30px;
        }

        .row span {
            font-size: 1.1rem;
            margin-right: 30px;
        }

        .row div:last-of-type {
            margin-right: 0;
        }

        a[href="/"] {
            margin-top: 10px;
        }

        @media screen and (max-width: 768px) {
            * {
                font-size: 20px;
            }

            .row-group {
                flex-direction: column;
            }

            .row-group div:nth-child(n+2) {
                margin-top: 10px;
            }
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
                <div><span style="font-weight: bolder">直播开始时间: ${incomeInfo.streamingTime()}</span></div>
            </div>
            <div class="row-group">
                <div>直播持续时间: ${incomeInfo.streamingDuration()}</div>
                <div>观看人数: ${incomeInfo.numberOfWatch()}</div>
                <div><span style="font-weight: bolder">收入: ${incomeInfo.income()}</span></div>
            </div>
        </div>
    </#list>
    <a href="/">返回首页</a>
</div>
</body>
</html>
