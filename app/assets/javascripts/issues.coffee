$(document).ready ->
  ISSUES.decorateRulesTab()
  ISSUES.decorateIssuesDetailTab()

#url = $(location).attr('href').substr(0, $(location).attr('href').length - 1)
#ISSUES.retrieveIssues url: url

selected_class="selected"

@ISSUES =

  decorateRulesTab: ->
    $("#rules tbody tr").each (i) ->
      if(i % 2 == 0)
        $(this).addClass("even")

  decorateIssuesDetailTab: ->
    $("#issues_details").hide()

    $("#rules tbody tr").on 'click', ->
      $("#issues_details").show()
      $("#issues_details tbody tr").hide()
      $("#rules tbody tr").removeClass(selected_class)

      $(this).addClass(selected_class)
      line_id = $($(this).children()[0]).children().text()

      $("#issues_details tbody tr").each (i) ->
        detail_id = $($(this).children()[0]).children().text()
        if(line_id == detail_id)
          $(this).show()


  ##{baseurl}/project/valuation/issues
  ###retrieveIssues : (baseurl) ->
    console.log baseurl
    $.ajax({
      type: 'GET',
      url: "http://localhost:9000/project/valuation/issues",
      dataType: "json",
      success: (data) ->
        ISSUES.displayRulesTab data
      error: (data) ->
        console.log "error"
    })###

  displayRulesTab: (data) ->
    #console.log data
