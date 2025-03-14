const poll = () => {
    if (new URLSearchParams(location.search).has('doNotCheckLogin'))
        return

    setTimeout(async () => {
        const response = await fetch('/checkLoggedIn')
        const result = await response.text()
        const isLoggedIn = result === 'true'
        console.log(`isLoggedIn: ${isLoggedIn}`)
        if (isLoggedIn) {
            location.replace('/finishLogin')
        } else {
            poll()
        }
    }, 5000)
}

onload = (event) => {
    poll()
}
