import { defineStore } from 'pinia'

export const useExamStore = defineStore('exam', {
  state: () => ({
    cutScreenCount: 0,
    maxCutScreen: 3,
    answers: {},
    markedQuestions: new Set()
  }),
  actions: {
    incrementCutScreen() {
      this.cutScreenCount++
    },
    resetCutScreen() {
      this.cutScreenCount = 0
    },
    saveAnswer(questionId, answer) {
      this.answers[questionId] = answer
    },
    loadAnswers(answerList) {
      if (!Array.isArray(answerList)) return
      answerList.forEach(item => {
        if (item.questionId && item.answer) {
          this.answers[item.questionId] = item.answer
        }
      })
    },
    toggleMark(questionId) {
      if (this.markedQuestions.has(questionId)) {
        this.markedQuestions.delete(questionId)
      } else {
        this.markedQuestions.add(questionId)
      }
    },
    resetAll() {
      this.cutScreenCount = 0
      this.answers = {}
      this.markedQuestions = new Set()
    }
  }
})
