class ContestEvents {
    eventId = 1
    events = [];

    addContest = (contest) => {
        this.events.push({
            id: this.eventId,
            type: 'contests',
            op: 'update',
            data: { ...contest },
        });
        this.eventId += 1;
    }
    
    addLang = (lang) => {
        this.events.push({
            id: this.eventId,
            type: 'languages',
            op: 'create',
            data: { ...lang },
        });
        this.eventId += 1;
    }
    
    addVerdicts = (verdict) => {
        this.events.push({
            id: this.eventId,
            type: "judgement-types",
            op: "create",
            data: { ...verdict },
        });
        this.eventId += 1;
    }
    
    addProblem = (problem) => {
        this.events.push({
            id: this.eventId,
            type: 'problems',
            op: 'create',
            data: { ...problem }
        });
        this.eventId += 1;
    }
    
    addTeam = (team) => {
        this.events.push({
            id: this.eventId,
            type: 'teams',
            op: 'create',
            data: { ...team },
        });
        this.eventId += 1;
    }
    
    addState = (state) => {
        this.events.push({
            id: this.eventId,
            type: 'state',
            op: 'update',
            data: { ...state },
        });
        this.eventId += 1;
    }
    
    addSubmission = (submission) => {
        this.events.push({ 
            id: this.eventId,
            type: 'submissions',
            op: 'create',
            data: { ...submission }
        });
        this.eventId += 1;
    }
    
    addJudgement = (judgement) => {
        this.events.push({ 
            id: this.eventId,
            type: 'judgements',
            op: 'create',
            data: { ...judgement }
        });
        this.eventId += 1;
    }    
}

module.exports = ContestEvents;