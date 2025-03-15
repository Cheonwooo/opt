from flask import Flask, request, jsonify
import re, numpy as np, torch
from konlpy.tag import Okt
from sentence_transformers import SentenceTransformer, util

app = Flask(__name__)

# 모델 로드
model = SentenceTransformer("jhgan/ko-sroberta-multitask")

def expand_context_with_filler(text, min_length=50):
    filler_context = "어, 음, 와, 아, 그런데, 이야, 어머나, 오, 이게 말이 되나, 흐음, 정말, 그렇군요!, 홀리, 뚱스, 띵킹, 파천, 아미, 돈황"
    if len(text) < min_length:
        return text + " " + filler_context
    return text

def l2_normalize(embeddings):
    return torch.nn.functional.normalize(embeddings, p=2, dim=1)

def remove_top_component(embeddings, n_components=1):
    embeddings_centered = embeddings - np.mean(embeddings, axis=0, keepdims=True)
    U, S, Vt = np.linalg.svd(embeddings_centered, full_matrices=False)
    top_components = Vt[:n_components]
    embeddings_processed = embeddings_centered - embeddings_centered.dot(top_components.T).dot(top_components)
    return embeddings_processed

def merge_tokens(tokens_with_pos):
    merged_tokens = []
    i = 0
    while i < len(tokens_with_pos):
        token, pos = tokens_with_pos[i]
        if token == "유" and i + 1 < len(tokens_with_pos) and tokens_with_pos[i+1][0] == "산소":
            merged_tokens.append(("유산소", "Noun"))
            i += 2
        elif token == "식이" and i + 1 < len(tokens_with_pos) and tokens_with_pos[i+1][0] == "요법":
            merged_tokens.append(("식이요법", "Noun"))
            i += 2
        else:
            merged_tokens.append((token, pos))
            i += 1
    return merged_tokens

@app.route('/extract_keywords', methods=['POST'])
def extract_keywords():
    data = request.get_json()
    if not data or 'text' not in data:
        return jsonify({'error': 'text 파라미터가 필요합니다.'}), 400

    origin_text = data['text']
    text = expand_context_with_filler(origin_text, min_length=50)
    text = re.sub(r"[^\w\s]", " ", text)

    okt = Okt()
    tokens_with_pos = okt.pos(text)
    tokens_with_pos = merge_tokens(tokens_with_pos)

    stopwords = {"은", "는", "이", "가", "을", "를", "의", "에", "에서", "으로", "와", "과", "도",
                 "입니다", "있습니다", "안녕하세요"}
    desired_pos = {"Noun", "Verb", "Adjective"}
    filtered_tokens = [word for word, pos in tokens_with_pos if pos in desired_pos and word not in stopwords]

    unigrams = filtered_tokens
    bigrams = [" ".join(filtered_tokens[i:i+2]) for i in range(len(filtered_tokens)-1)]
    phrases = list(set(unigrams + bigrams))

    synonyms_dict = {
        "바디프로필": ["바디빌딩", "피지컬", "몸 만들기"],
        "보디": ["바디", "바디빌딩"],
        "바벨": ["바벨운동"],
        "보디빌딩": ["바디빌딩"],
        "자세교정": ["체형교정"],
        "중심근육": ["코어"],
        "균형":["코어"],
        "균형":["체형교정"],
        "다이어트": ["체중관리"],
        "몸무게 감량": ["체중관리"],
        "체중관리": ["다이어트"],
        "체중감량": ["체중관리"],
        "카디오": ["유산소"],
        "달리기": ["유산소"],
        "러닝":["유산소"],
        "런닝":["유산소"],
        "몸 만들기": ["바디빌딩"],
        "살 빼기": ["다이어트"],
        "몸무게": ["다이어트"],
        "영양": ["식이요법"],
        "영양 관리": ["식이요법"],
        "식단": ["식이요법"],
        "식이요법": ["식단", "영양", "영양관리"],
        "교정": ["체형교정"],
        "체형": ["체형교정"],
        "근육 강화": ["근력"],
        "근육":["근력"],
        "웨이트":["웨이트"],
        "심폐": ["유산소"]
    }
    extended_phrases = set(phrases)
    for phrase in phrases:
        for key, syns in synonyms_dict.items():
            if key in phrase:
                for syn in syns:
                    extended_phrases.add(syn)
    phrases = list(extended_phrases)

    topics = ["바벨운동", "바디빌딩", "스트레칭", "식이요법", "유산소", "재활", "체중관리", "체형교정", "코어", "피트니스", "근력"]

    phrase_embeddings = model.encode(phrases, convert_to_tensor=True)
    topic_embeddings = model.encode(topics, convert_to_tensor=True)

    phrase_embeddings = l2_normalize(phrase_embeddings)
    topic_embeddings = l2_normalize(topic_embeddings)

    phrase_np = phrase_embeddings.cpu().numpy()
    topic_np = topic_embeddings.cpu().numpy()

    phrase_np = remove_top_component(phrase_np, n_components=1)
    topic_np = remove_top_component(topic_np, n_components=1)

    phrase_embeddings = torch.tensor(phrase_np)
    topic_embeddings = torch.tensor(topic_np)

    cosine_matrix = util.cos_sim(phrase_embeddings, topic_embeddings)
    num_topics_val = len(topics)

    score_threshold = 0.41
    flattened_scores = cosine_matrix.flatten()
    qualified_indices = torch.nonzero(flattened_scores >= score_threshold).flatten()

    selected = []
    if qualified_indices.numel() > 0:
        sorted_qualified = qualified_indices[torch.argsort(flattened_scores[qualified_indices], descending=True)]
        selected_topics = set()
        for idx in sorted_qualified:
            topic_idx = idx.item() % num_topics_val
            if topics[topic_idx] in selected_topics:
                continue
            selected.append(topics[topic_idx])
            selected_topics.add(topics[topic_idx])
            if len(selected) >= 5:
                break

    return jsonify({'keywords': selected})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
