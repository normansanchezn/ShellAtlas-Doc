import Testing
@testable import SD_Presentation

@Test func detectsSpanishAssistantLanguage() {
    let language = AssistantResponseLanguage.detect(from: "¿Cuál es el proceso de un end of sprint build?")

    #expect(language == .spanish)
    #expect(language.sourcesTitle == "Fuentes")
    #expect(language.sourceUnavailableTitle == "Fuente no disponible")
}

@Test func detectsEnglishAssistantLanguage() {
    let language = AssistantResponseLanguage.detect(from: "What is the rewards flow?")

    #expect(language == .english)
    #expect(language.sourcesTitle == "Sources")
    #expect(language.sourceUnavailableTitle == "Source unavailable")
}
