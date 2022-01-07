/*
A script to generate input file for Resolver
Usage: node index.js <contest code>
Node: This script doesn't support team user.
*/
const util = require('util');
const mysql = require('mysql');
const ContestEvents = require('./contestEvents');

const connection = mysql.createConnection({
    host     : process.env.DBHOST || 'localhost',
    user     : process.env.DBUSER || 'root',
    password : process.env.DBPASS || 'test',
    database : 'spoj'
});

connection.connect();

connection.query = util.promisify(connection.query);

const contestCode = process.argv[2];
const freezeDurationInMS = 3600000;
const freezeDurationInHr = "1:00:00.000";
const contestDuration = '3:00:00';
const contestDurationInMs = 3 * 3600000;

const verdicts = [
    {"id":"AC","name":"correct","penalty":false,"solved":true},
    {"id":"CE","name":"compiler error","penalty":false,"solved":false},
    {"id":"MLE","name":"memory limit","penalty":true,"solved":false},
    {"id":"NO","name":"no output","penalty":true,"solved":false},
    {"id":"OLE","name":"output limit","penalty":true,"solved":false},
    {"id":"PE","name":"presentation error","penalty":true,"solved":false},
    {"id":"RTE","name":"run error","penalty":true,"solved":false},
    {"id":"TLE","name":"timelimit","penalty":true,"solved":false},
    {"id":"WA","name":"wrong answer","penalty":true,"solved":false}
];

const verdicts_map = {
    15: 'AC',
    14: 'WA',
    13: 'TLE',
    12: 'RTE',
    11: 'CE'
}

function getFlag(countryCode) {
    const codePoints = countryCode
      .toUpperCase()
      .split('')
      .map(char =>  127397 + char.charCodeAt());
    return String.fromCodePoint(...codePoints);
}

function getStar(rating)
{
    rating = rating || 0;
    rating = parseInt(rating);
    const bands = {
        "7": {"low": 2500, "high": 50000},
        "6": {"low": 2200, "high": 2500},
        "5": {"low": 2000, "high": 2200},
        "4": {"low": 1800, "high": 2000},
        "3": {"low": 1600, "high": 1800},
        "2": {"low": 1400, "high": 1600},
        "1": {"low": 0, "high": 1400}
    };
    
    let star = 0;
    Object.keys(bands).forEach(key => {
        const value = bands[key];
        if (rating >= value.low && rating < value.high) {
            star = parseInt(key);
        }
    });

    return star;
}

(async () => {
    try {
        const contestEvents = new ContestEvents();
        let contest = { code: contestCode };
        const contests = await connection.query(`select id, name, start_date, end_date, TIMEDIFF(end_date, start_date) as duration from contests where code = '${contest.code}'`);
        contest = {
            formal_name: contests[0].name,
            penalty_time: 10, // minutes
            start_time: contests[0].start_date,
            duration: contestDuration,
            scoreboard_freeze_duration: freezeDurationInHr,
            id: contests[0].id,
            name: contests[0].name
        };
        contestEvents.addContest(contest);
        contest.end_time = contests[0].end_date;

        verdicts.forEach(v => { contestEvents.addVerdicts(v) });

        const langs = await connection.query(`select * from langs`);
        langs.forEach(lang => {
            contestEvents.addLang({
                id: lang.full_name,
                name: lang.full_name,
                extensions: [lang.extension],
            });
        });

        const problems = await connection.query(`select * from contest_problems cp inner join problems p on p.id = cp.problem_id where contest_id = ${contest.id}`);
        problems.forEach((problem, idx) => {
            contestEvents.addProblem({
                ordinal: idx+1,
                id: problem.problem_id,
                label: idx+1,
                name: problem.code,
                test_data_count: 5
            });
        });

        const users = await connection.query(`SELECT user_id FROM codechef.contest_registrations WHERE contest_id = ${contest.id} limit 100`);
        const userIds = []
        users.forEach(user => {
            userIds.push(user.user_id);
        })
        // console.log(JSON.stringifyuserIds);

        const teams = await connection.query(`select cr.country_code, u.uid, u.name, acr.rating from codechef.users u
        left join codechef.user_information ui on u.uid = ui.uid
        left join codechef.city c on c.city_id = ui.city_id
        left join codechef.state s on s.state_id = c.state_id
        left join codechef.country cr on cr.country_id = s.country_id
        left join codechef.all_contest_ranks acr on acr.uid = u.uid
        where u.uid in (${userIds.join(',')})`);
        teams.forEach(u => {
            // console.log(getStar(u.rating));
            const team = {
                id: u.uid,
                name: getFlag(u.country_code || 'IN') + ' ' + String.fromCodePoint(0xe000 + getStar(u.rating)) + u.name,
            };
            contestEvents.addTeam(team);
        });

        const submissions = await connection.query(`select * from user_programs where contest_id = ${contest.id} and disqualified = 0 and user_id in (${userIds.join(',')})`);

        let flag = true;
        let state = {
            started: new Date(contest.start_time).toISOString(), 
            ended: null,
            frozen: null,
            thawed: null,
            finalized: null,
            end_of_updates: null
        };
        contestEvents.addState(state);

        submissions.forEach(row => {
            const submission_date = new Date(row.submission_date);
            // console.log(submission_date.getTime() + freezeDurationInMS, (new Date(contest.start_time).getTime()) + contestDurationInMs);
            if (flag && submission_date.getTime() + freezeDurationInMS > (new Date(contest.start_time).getTime()) + contestDurationInMs) {
                state.frozen = submission_date.toISOString();
                contestEvents.addState(state);
                flag = false;
            }

            const diff_time = parseInt((submission_date - new Date(contest.start_time))/1000);
            const diff_h = parseInt(diff_time/3600);
            const diff_m = parseInt(diff_time/60)%60;
            const diff_s = parseInt(diff_time%60);
            const diff_str = `${diff_h}:${String(diff_m).padStart(2, '0')}:${String(diff_s).padStart(2, '0')}.000`;

            const submission = {
                language_id: 'C++',
                time: submission_date.toISOString(),
                contest_time: diff_str,
                id: row.id,
                team_id: row.user_id,
                problem_id: row.problem_id,
                entry_point: row.problem_id,
                files: [
                    {
                        href: 'contests/files/' + row.id
                    }
                ]
            };
            contestEvents.addSubmission(submission);

            const judgement = {
                start_time: submission_date.toISOString(),
                start_contest_time: diff_str,
                end_time: submission_date.toISOString(),
                end_contest_time: diff_str,
                id: `${row.id}0`,
                submission_id: row.id,
                judgement_type_id: verdicts_map[row.result_code] || verdicts_map[11]
            }
            contestEvents.addJudgement(judgement);
        });

        state.ended = new Date(contest.end_time).toISOString();
        contestEvents.addState(state);
        state.finalized = state.ended;
        contestEvents.addState(state);
        state.thawed = state.ended;
        contestEvents.addState(state);
        state.end_of_updates = state.ended;
        contestEvents.addState(state);
    
        connection.end();
        contestEvents.events.forEach(e => {
            console.log(JSON.stringify(e))
        });
    } catch (err) {
        console.error(err);
    }
})();
