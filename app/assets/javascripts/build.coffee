$(document).ready ->
  BUILD.eventSource()

@BUILD =

  eventSource: ->
    if (!!window.EventSource)
      source = new EventSource('/build/test')

      ###source.onmessage = (event) ->
        console.log "temoin"
        console.log JSON.parse(event.data)###

      source.addEventListener 'testEvent', (e) ->             # 'message' ecoute tous les event
        data = JSON.parse e.data
        console.log data

      source.addEventListener 'projectCloned', (e) ->
        $("#projectCloned").show()

      source.addEventListener 'scctDone', (e) ->
        $("#scctDone").show()

      source.addEventListener 'checkstyleDone', (e) ->
        $("#checkstyleDone").show()

      source.addEventListener 'error', (e) ->
        console.log e

     else
      console.log "your browser don't support Server Sent Event feature ! Please use real Browser to use this app !"

# http://www.html5rocks.com/en/tutorials/eventsource/basics/