<!DOCTYPE html>
<html lang="zh-Hans">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width">
    <title>直播收入</title>
    <link rel="stylesheet" href="../static/common.css">
    <style>
        * {
            font-size: 18px;
        }

        .container {
            width: 80vw;
        }

        h1 {
            font-size: 1.5em;
        }

        .row {
            display: flex;
            flex-direction: column;
            padding: 10px 10px 10px 0;
        }

        .row-group {
            display: flex;
            align-items: baseline;
        }

        .row-group:nth-child(2) {
            margin-top: 10px;
        }

        .item {
            line-height: 1.2;
        }

        .row:nth-of-type(n+2) {
            border-top: 1px solid rgba(178, 178, 178, .5);
        }

        .row span {
            font-size: 1.1rem;
        }

        .title {
            flex-basis: 60%;
        }

        .title > span {
            background: rgba(167, 167, 167, 0.18);
            padding: 3px 4px 5px 0;
        }

        .duration {
            flex-basis: 40%;
        }

        .audience-count {
            flex-basis: 20%;
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
    <h1>近期直播收入</h1>
    <#list incomeInfoList as incomeInfo>
        <div class="row">
            <div class="row-group">
                <div class="item title"><span>直播标题: ${incomeInfo.streamingTitle()}</span></div>
                <div class="item start-time"><span
                            style="font-weight: bolder">直播开始时间: ${incomeInfo.streamingTime()}</span></div>
            </div>
            <div class="row-group">
                <div class="item duration">直播持续时间: ${incomeInfo.streamingDuration()}</div>
                <div class="item audience-count">观看人数: ${incomeInfo.numberOfWatch()}</div>
                <div class="item income"><span style="font-weight: bolder">收入: ${incomeInfo.income()}</span></div>
            </div>
        </div>
    </#list>
    <a href="/">返回首页</a>
</div>
</body>
</html>
